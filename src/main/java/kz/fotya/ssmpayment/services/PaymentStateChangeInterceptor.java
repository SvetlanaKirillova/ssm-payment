package kz.fotya.ssmpayment.services;

import kz.fotya.ssmpayment.domain.Payment;
import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import kz.fotya.ssmpayment.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor
        extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state,
                               Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        System.out.println("FROM INTERCEPTOR: msg = " + message);
        Optional.ofNullable(message).ifPresent( msg -> {
            Optional.ofNullable((Long) msg.getHeaders().get(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .ifPresent( paymentId -> {
                        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
                        Optional.of(paymentOptional).ifPresent(pmnt -> {
                            Payment payment = pmnt.get();
                            payment.setState(state.getId());
                            paymentRepository.save(payment);
                        });
                    });
        });
    }
}
