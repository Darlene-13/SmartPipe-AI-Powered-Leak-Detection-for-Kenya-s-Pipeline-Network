package io.github.darlene.leakdetectionapplication.repository;


import io.github.darlene.sessionauth.entity.UserRole;
import io.github.darlene.sessionauth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>{

    Optional<User> findByUsername (String username);

    Optional<User> findByUserRole (UserRole userRole);

}