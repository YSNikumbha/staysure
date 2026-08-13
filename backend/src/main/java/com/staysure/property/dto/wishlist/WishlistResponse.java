package com.staysure.property.dto.wishlist;

import com.staysure.property.dto.discovery.PublicPgCardResponse;

import java.time.LocalDateTime;

public record WishlistResponse(
        Long id,
        PublicPgCardResponse property,
        LocalDateTime createdAt
) {
}
