package com.chou.app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String username);

    @Query("SELECT u FROM User u WHERE u.email=?1 ")
    User findUserByEmail(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id IN :userIds")
    void deleteUsersByIdIn(@Param("userIds") List<Long> userIds);
    public List<User> findUserByCreatedDateBeforeAndEnabledIsFalse( LocalDateTime checkDate);

    @Query("SELECT MAX(u.orderNumber) FROM User u")
    Integer findMaxOrderNumber();

    List<User> findAllByOrderByOrderNumberAsc();


}
