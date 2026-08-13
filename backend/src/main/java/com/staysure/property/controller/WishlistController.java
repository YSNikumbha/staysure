package com.staysure.property.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.property.dto.wishlist.WishlistResponse;
import com.staysure.property.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Wishlist loaded",
                wishlistService.list(SecurityUtils.currentUserId())));
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(@PathVariable Long propertyId,
                                                             HttpServletRequest servletRequest) {
        WishlistResponse response = wishlistService.add(
                SecurityUtils.currentUserId(),
                propertyId,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG saved to wishlist", response));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long propertyId,
                                                    HttpServletRequest servletRequest) {
        wishlistService.remove(SecurityUtils.currentUserId(), propertyId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("PG removed from wishlist"));
    }
}
