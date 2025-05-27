package com.playdata.evalservice.eval.repository;

import com.playdata.evalservice.eval.entity.Eval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvalRepository extends JpaRepository<Eval, Long> {

    Optional<Eval> findByProductIdAndUserId(Long productId, Long userId);

}
