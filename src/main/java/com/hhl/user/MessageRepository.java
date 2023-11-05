package com.hhl.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long>{
	@Query(value="select * from message where user_id = ?1 and is_read = 0 order by time desc", nativeQuery = true)
	List<Message> findMessagesByUser(long userId);

}
