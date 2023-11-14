package com.hhl.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExnessRepository extends JpaRepository<Exness, Long> {
	Optional<Exness> findByExness(String exness);

	List<Exness> findByUser(User user);

	@Query(value="select * from exness where refferal = ?1", nativeQuery = true)
	List<Exness> findByRefferal(String refferal);

}
