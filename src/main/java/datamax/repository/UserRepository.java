package datamax.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import datamax.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

  boolean existsByEmail(String username);

  AppUser findByEmail(String username);

  @Transactional
  void deleteByEmail(String username);

}
