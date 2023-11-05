package com.hhl.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	@Query(value="select * from transaction where exness_id = ?1 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByExnessId(String exness);

	@Query(value="select * from transaction where exness_id = ?1 and type = ?2 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByExnessIdAndType(String exness, String type);
	
	@Query(value="select * from transaction where amount = ?1 and time = ?2 and exness_id = ?3 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByAmountAndTimeAndExness(double amount, long time, String exness);
}
