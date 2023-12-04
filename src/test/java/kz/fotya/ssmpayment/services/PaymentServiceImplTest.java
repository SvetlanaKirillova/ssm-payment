package kz.fotya.ssmpayment.services;

import kz.fotya.ssmpayment.domain.Payment;
import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import kz.fotya.ssmpayment.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    PaymentService paymentService;

    Payment payment;
    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(BigDecimal.valueOf(15.00)).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        Optional<Payment> payment1Optional = paymentRepository.findById(savedPayment.getId());

        System.out.println(sm.getState().getId());
        System.out.println(payment1Optional);

    }


    @Transactional
    @Test
    void authPayment() {
        preAuth();
        System.out.println("Authorization is starting...");
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        if (sm.getState().getId().equals(PaymentState.PRE_AUTH)){
            paymentService.authPayment(savedPayment.getId());
        }

        Optional<Payment> payment1Optional = paymentRepository.findById(savedPayment.getId());
        System.out.println("State machine' state = " + sm.getState().getId());
        System.out.println("Saved payment:" + payment1Optional);

    }
}