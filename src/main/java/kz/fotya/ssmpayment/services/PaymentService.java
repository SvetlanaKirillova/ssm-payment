package kz.fotya.ssmpayment.services;

import kz.fotya.ssmpayment.domain.Payment;
import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuthPayment(Long paymentId);
}
