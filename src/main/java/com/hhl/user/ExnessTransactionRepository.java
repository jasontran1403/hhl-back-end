package com.hhl.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExnessTransactionRepository extends JpaRepository<ExnessTransaction, Long>{
	Optional<ExnessTransaction> findByTransactionExness(String transactionExness);
}

