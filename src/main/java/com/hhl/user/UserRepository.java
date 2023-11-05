package com.hhl.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);

	Optional<User> findByCode(String code);

	@Query(value = "select * from _user where refferal = ?1", nativeQuery = true)
	List<User> findByRefferal(String email);
	
	@Query(value = "select * from _user where email = ?1", nativeQuery = true)
	User getByEmail(String email);

	@Query(value="select * from _user where branch_name = ?1", nativeQuery = true)
	List<User> getUsersByBranchName(String branchName);
}
