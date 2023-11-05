package com.hhl.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryRepository extends JpaRepository<History, Long> {
//  Optional<History> findByUser(User user);
	
	@Query(value="select * from history where receiver = ?1 order by time asc", nativeQuery=true)
	List<History> findByReceive(String email);
	
	@Query(value="select * from history where receiver = ?1 and time >= ?2 and time <= ?3 order by time asc", nativeQuery=true)
	List<History> findByReceiveAndTimeRange(String email, long from, long to);
	
}
