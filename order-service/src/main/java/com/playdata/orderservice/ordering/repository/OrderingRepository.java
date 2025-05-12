package com.playdata.orderservice.ordering.repository;

import com.playdata.orderservice.ordering.entity.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderingRepository extends JpaRepository<Ordering, Long> {

    List<Ordering> findByUserId(Long userId);

}
