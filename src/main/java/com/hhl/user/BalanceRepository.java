package com.hhl.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRepository extends JpaRepository<Balance, Long>{
	@Query(value="select * from balance where exness_id = ?1 order by time asc", nativeQuery = true)
	List<Balance> findByExness(String exness);
	
	@Query(value="select * from balance where exness_id = ?1 order by time asc", nativeQuery = true)
	List<Balance> findByExnessByTime(String exness, long from, long to);
	
	@Query(value="select * from balance where amount = ?1 and time = ?2 and exness_id = ?3 order by time desc", nativeQuery = true)
	List<Balance> findByAmountAndTimeAndExness(double amount, long time, String exness);
	
	@Query(value="select * from balance where time = ?1 and exness_id = ?2 order by time desc", nativeQuery = true)
	List<Balance> findByTimeAndExness(long time, String exness);
}
