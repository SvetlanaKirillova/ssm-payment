package kz.fotya.ssmpayment.config;

import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import kz.fotya.ssmpayment.services.PaymentService;
import kz.fotya.ssmpayment.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states)
            throws Exception {

        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.AUTH_ERROR)
                .end(PaymentState.PRE_AUTH_ERROR);

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions)
            throws Exception {

        transitions.withExternal()
                .source(PaymentState.NEW).target(PaymentState.NEW)
                .event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction()).guard(paymentIdGuard())
                .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
                .event(PaymentEvent.PRE_AUTH_APPROVE)
                .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR)
                .event(PaymentEvent.PRE_AUTH_DECLINE)
                .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH)
                .event(PaymentEvent.AUTHORIZE).action(authorizeAction())
                .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH)
                .event(PaymentEvent.AUTH_APPROVE).action(authorizeAction())
                .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR)
                .event(PaymentEvent.AUTH_DECLINE).action(authorizeAction());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(
                    State<PaymentState, PaymentEvent> from,
                    State<PaymentState, PaymentEvent> to
            ) {
                log.info(String.format("STATE CHANGED FROM: %s TO: %s", from.getId(), to.getId()));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return context -> {
            return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }

    public Action<PaymentState, PaymentEvent> preAuthAction(){
        return context -> {
            System.out.println("preAuth action was called!");
            if (new Random().nextInt(10) < 8 ){
                System.out.println("PreAuth Payment was approved!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVE)
                                .setHeader(
                                        PaymentServiceImpl.PAYMENT_ID_HEADER,
                                        context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)
                                ).build());
            }else {
                System.out.println("PreAuth Payment was declined!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINE)
                                .setHeader(
                                        PaymentServiceImpl.PAYMENT_ID_HEADER,
                                        context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)
                                ).build());
            }
        };
    }

    public Action<PaymentState, PaymentEvent> authorizeAction(){
        return context -> {
            System.out.println("Authorization action was called...");
            if (new Random().nextInt(10) <= 8){
                System.out.println("Payment Authorization is approved!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVE).setHeader(
                                PaymentServiceImpl.PAYMENT_ID_HEADER,
                                context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)
                        ).build()
                );
            } else {
                System.out.println("Payment Authorization is declined!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINE).setHeader(
                                PaymentServiceImpl.PAYMENT_ID_HEADER,
                                context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)
                        ).build()
                );
            }
        };
    }
}
