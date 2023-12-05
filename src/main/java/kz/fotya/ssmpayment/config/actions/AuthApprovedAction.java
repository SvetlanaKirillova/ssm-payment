package kz.fotya.ssmpayment.config.actions;

import kz.fotya.ssmpayment.domain.PaymentEvent;
import kz.fotya.ssmpayment.domain.PaymentState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class AuthApprovedAction implements Action<PaymentState, PaymentEvent> {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> stateContext) {
        System.out.println("NOTIFICATION! Payment was approved!");
    }
}
