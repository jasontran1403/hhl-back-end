package com.hhl.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommissionRepository extends JpaRepository<Commission, Long>{
	@Query(value="select * from commission where exness_id = ?1", nativeQuery=true)
	List<Commission> getCommissionByExnessId(String exnessId);
	
	@Query(value="select * from commission where exness_id = ?1 order by time asc", nativeQuery=true)
	List<Commission> getCommissionByExnessIdAndTime(String exnessId, long from, long to);

}
