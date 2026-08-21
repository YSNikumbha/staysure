package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.FoodFeedbackRequest;
import com.staysure.operations.dto.FoodFeedbackResponse;
import com.staysure.operations.dto.FoodMenuResponse;
import com.staysure.operations.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserFoodController {

    private final FoodService foodService;

    public UserFoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/food-menus/today")
    public ResponseEntity<ApiResponse<List<FoodMenuResponse>>> today() {
        return ResponseEntity.ok(ApiResponse.success("Food menu loaded",
                foodService.listMenusForUser(SecurityUtils.currentUserId(), LocalDate.now())));
    }

    @GetMapping("/food-menus")
    public ResponseEntity<ApiResponse<List<FoodMenuResponse>>> byDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Food menu loaded",
                foodService.listMenusForUser(SecurityUtils.currentUserId(), date)));
    }

    @PostMapping("/food-feedback")
    public ResponseEntity<ApiResponse<FoodFeedbackResponse>> feedback(@Valid @RequestBody FoodFeedbackRequest request,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Food feedback submitted",
                foodService.submitFeedback(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }
}
