package com.staysure.property.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.property.dto.discovery.PublicPgCardResponse;
import com.staysure.property.dto.wishlist.WishlistResponse;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Wishlist;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.WishlistRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PgPropertyRepository pgPropertyRepository;
    private final UserService userService;
    private final PublicPgDiscoveryService publicPgDiscoveryService;
    private final AuditService auditService;

    public WishlistService(WishlistRepository wishlistRepository,
                           PgPropertyRepository pgPropertyRepository,
                           UserService userService,
                           PublicPgDiscoveryService publicPgDiscoveryService,
                           AuditService auditService) {
        this.wishlistRepository = wishlistRepository;
        this.pgPropertyRepository = pgPropertyRepository;
        this.userService = userService;
        this.publicPgDiscoveryService = publicPgDiscoveryService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> list(Long userId) {
        User user = userService.getUser(userId);
        List<Wishlist> wishlists = wishlistRepository.findAllByUserOrderByCreatedAtDesc(user);
        List<PgProperty> properties = wishlists.stream()
                .map(wishlist -> wishlist.getProperty())
                .filter(property -> publicPgDiscoveryService.isPubliclyVisible(property))
                .toList();
        Map<Long, PublicPgCardResponse> cards = new java.util.HashMap<>();
        for (PublicPgCardResponse card : publicPgDiscoveryService.cardsForProperties(properties)) {
            cards.put(card.id(), card);
        }
        return wishlists.stream()
                .filter(wishlist -> cards.containsKey(wishlist.getProperty().getId()))
                .map(wishlist -> new WishlistResponse(wishlist.getId(), cards.get(wishlist.getProperty().getId()), wishlist.getCreatedAt()))
                .toList();
    }

    @Transactional
    public WishlistResponse add(Long userId, Long propertyId, String ipAddress) {
        User user = userService.getUser(userId);
        PgProperty property = publicProperty(propertyId);
        if (wishlistRepository.existsByUserAndProperty(user, property)) {
            throw new DuplicateResourceException("PG already saved to wishlist", "WISHLIST_ALREADY_EXISTS");
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProperty(property);
        Wishlist saved = wishlistRepository.save(wishlist);
        auditService.log(user, "WISHLIST_ADDED", "WISHLIST", "PgProperty", property.getId(),
                "PG added to wishlist", null, null, ipAddress);
        return list(userId).stream()
                .filter(item -> item.id().equals(saved.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wishlist item not found", "WISHLIST_NOT_FOUND"));
    }

    @Transactional
    public void remove(Long userId, Long propertyId, String ipAddress) {
        User user = userService.getUser(userId);
        PgProperty property = publicProperty(propertyId);
        Wishlist wishlist = wishlistRepository.findByUserAndProperty(user, property)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wishlist item not found", "WISHLIST_NOT_FOUND"));
        wishlistRepository.delete(Objects.requireNonNull(wishlist, "wishlist must not be null"));
        auditService.log(user, "WISHLIST_REMOVED", "WISHLIST", "PgProperty", property.getId(),
                "PG removed from wishlist", null, null, ipAddress);
    }

    private PgProperty publicProperty(Long propertyId) {
        Long id = Objects.requireNonNull(propertyId, "propertyId must not be null");
        PgProperty property = pgPropertyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PG not found", "PUBLIC_PG_NOT_FOUND"));
        if (!publicPgDiscoveryService.isPubliclyVisible(property)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PG not found", "PUBLIC_PG_NOT_FOUND");
        }
        return property;
    }
}
