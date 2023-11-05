package com.hhl.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExnessRepository extends JpaRepository<Exness, Long> {
  Optional<Exness> findByExness(String exness);
  List<Exness> findByUser(User user);
  
}
