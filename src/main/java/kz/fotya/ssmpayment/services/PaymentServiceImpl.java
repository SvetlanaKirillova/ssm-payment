package kz.fotya.ssmpayment.services;

import kz.fotya.ssmpayment.domain.Payment;
import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import kz.fotya.ssmpayment.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    public final static String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        if (sm != null){
            sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        }
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        if (sm != null){
            sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        }
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuthPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        if (sm != null){
            sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINE);
        }
        return sm;
    }

    private void sendEvent(Long paymentId,
                           StateMachine<PaymentState,PaymentEvent> sm,
                           PaymentEvent event){
        Message<PaymentEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);

    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);

        if (paymentOptional.isPresent()){
            StateMachine<PaymentState, PaymentEvent> sm =
                    stateMachineFactory.getStateMachine(Long.toString(paymentOptional.get().getId()));
            sm.stop();

            sm.getStateMachineAccessor().doWithAllRegions( sma -> {
                sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                sma.resetStateMachine(
                        new DefaultStateMachineContext<>(
                                paymentOptional.get().getState(),
                                null,
                                null,
                                null)
                );
            });
            sm.start();
            return sm;
        } else {
            log.error("There are no payment with id="+paymentId+" in repository.");
            return null;
        }

    }
}
