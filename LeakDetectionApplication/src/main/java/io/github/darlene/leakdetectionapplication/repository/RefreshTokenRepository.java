package io.github.darlene.leakdetectionapplication.repository;


import io.github.darlene.leakdetectionapplication.domain.RefreshToken;
import io.github.darlene.leakdetectionapplication.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user)

}