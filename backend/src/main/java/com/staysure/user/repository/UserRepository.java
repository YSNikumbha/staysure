package com.staysure.user.repository;

import com.staysure.common.enums.RoleName;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("select distinct u from User u join u.roles r where r.name = :roleName")
    List<User> findAllByRole(@Param("roleName") RoleName roleName);
}
