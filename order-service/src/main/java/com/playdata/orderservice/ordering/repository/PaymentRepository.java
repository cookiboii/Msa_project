package com.playdata.orderservice.ordering.repository;

import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.entity.Payment;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPartnerOrderId(String partnerOrderId);

    @Query("SELECT p FROM Payment p WHERE p.ordering.id = :orderId")
    Optional<Payment> findByOrderingId(@Param("orderId") Long orderId);
}
