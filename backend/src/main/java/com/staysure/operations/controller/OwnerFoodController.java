package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.FoodFeedbackResponse;
import com.staysure.operations.dto.FoodMenuRequest;
import com.staysure.operations.dto.FoodMenuResponse;
import com.staysure.operations.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerFoodController {

    private final FoodService foodService;

    public OwnerFoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/food-menus")
    public ResponseEntity<ApiResponse<List<FoodMenuResponse>>> listMenus() {
        return ResponseEntity.ok(ApiResponse.success("Food menus loaded", foodService.listMenusForOwner(SecurityUtils.currentUserId())));
    }

    @PostMapping("/food-menus")
    public ResponseEntity<ApiResponse<FoodMenuResponse>> createMenu(@Valid @RequestBody FoodMenuRequest request,
                                                                    HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Food menu created",
                foodService.createMenu(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/food-menus/{id}")
    public ResponseEntity<ApiResponse<FoodMenuResponse>> updateMenu(@PathVariable Long id,
                                                                    @Valid @RequestBody FoodMenuRequest request,
                                                                    HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Food menu updated",
                foodService.updateMenu(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/food-feedback")
    public ResponseEntity<ApiResponse<List<FoodFeedbackResponse>>> feedback(@RequestParam(required = false) Long propertyId) {
        return ResponseEntity.ok(ApiResponse.success("Food feedback loaded",
                foodService.listFeedbackForOwner(SecurityUtils.currentUserId(), propertyId)));
    }
}
