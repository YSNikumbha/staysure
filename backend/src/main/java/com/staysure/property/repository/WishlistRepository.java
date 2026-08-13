package com.staysure.property.repository;

import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Wishlist;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findAllByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndProperty(User user, PgProperty property);

    Optional<Wishlist> findByUserAndProperty(User user, PgProperty property);
}
