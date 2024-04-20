package com.chou.app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
    void deleteTokensByUserId(@Param("userId") Long userId);

    void deleteByUser(User user);

}
