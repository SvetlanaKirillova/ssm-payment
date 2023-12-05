package kz.fotya.ssmpayment.config.actions;

import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import kz.fotya.ssmpayment.services.PaymentServiceImpl;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
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

    }
}
