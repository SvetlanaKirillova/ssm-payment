package kz.fotya.ssmpayment.repositories;

import kz.fotya.ssmpayment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
