package com.staysure.operations.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.operations.dto.FoodFeedbackRequest;
import com.staysure.operations.dto.FoodFeedbackResponse;
import com.staysure.operations.dto.FoodMenuRequest;
import com.staysure.operations.dto.FoodMenuResponse;
import com.staysure.operations.entity.FoodFeedback;
import com.staysure.operations.entity.FoodMenu;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.FoodFeedbackRepository;
import com.staysure.operations.repository.FoodMenuRepository;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FoodService {

    private final FoodMenuRepository foodMenuRepository;
    private final FoodFeedbackRepository foodFeedbackRepository;
    private final OperationAccessService accessService;
    private final AuditService auditService;
    private final OperationMapper mapper;

    public FoodService(FoodMenuRepository foodMenuRepository,
                       FoodFeedbackRepository foodFeedbackRepository,
                       OperationAccessService accessService,
                       AuditService auditService,
                       OperationMapper mapper) {
        this.foodMenuRepository = foodMenuRepository;
        this.foodFeedbackRepository = foodFeedbackRepository;
        this.accessService = accessService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<FoodMenuResponse> listMenusForOwner(Long ownerUserId) {
        return foodMenuRepository.findAllByOwner(accessService.owner(ownerUserId)).stream().map(mapper::toFoodMenu).toList();
    }

    @Transactional
    public FoodMenuResponse createMenu(Long ownerUserId, FoodMenuRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        OwnerProfile owner = accessService.owner(ownerUserId);
        PgProperty property = accessService.ownedProperty(request.propertyId(), owner);
        if (foodMenuRepository.findByPropertyAndMenuDateAndMealType(property, request.menuDate(), request.mealType()).isPresent()) {
            throw new DuplicateResourceException("Food menu already exists for this meal", "FOOD_MENU_ALREADY_EXISTS");
        }
        FoodMenu menu = new FoodMenu();
        applyMenu(menu, property, request);
        FoodMenu saved = foodMenuRepository.save(menu);
        auditService.log(actor, "FOOD_MENU_CREATED", "OPERATIONS", "FoodMenu", saved.getId(),
                "Food menu created", null, saved.getMealType().name(), ipAddress);
        return mapper.toFoodMenu(saved);
    }

    @Transactional
    public FoodMenuResponse updateMenu(Long ownerUserId, Long menuId, FoodMenuRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        OwnerProfile owner = accessService.owner(ownerUserId);
        FoodMenu menu = ownerMenu(owner, menuId);
        PgProperty property = accessService.ownedProperty(request.propertyId(), owner);
        foodMenuRepository.findByPropertyAndMenuDateAndMealType(property, request.menuDate(), request.mealType())
                .filter(existing -> !existing.getId().equals(menuId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Food menu already exists for this meal", "FOOD_MENU_ALREADY_EXISTS");
                });
        applyMenu(menu, property, request);
        FoodMenu saved = foodMenuRepository.save(menu);
        auditService.log(actor, "FOOD_MENU_UPDATED", "OPERATIONS", "FoodMenu", saved.getId(),
                "Food menu updated", null, saved.getMealType().name(), ipAddress);
        return mapper.toFoodMenu(saved);
    }

    @Transactional(readOnly = true)
    public List<FoodMenuResponse> listMenusForUser(Long userId, LocalDate date) {
        TenantProfile tenant = accessService.activeTenant(userId);
        return foodMenuRepository.findAllByPropertyAndMenuDateOrderByMealTypeAsc(
                tenant.getProperty(),
                date == null ? LocalDate.now() : date
        ).stream().map(mapper::toFoodMenu).toList();
    }

    @Transactional
    public FoodFeedbackResponse submitFeedback(Long userId, FoodFeedbackRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        TenantProfile tenant = accessService.activeTenant(userId);
        if (request.rating() < 1 || request.rating() > 5) {
            throw new BusinessRuleException("Food rating must be between 1 and 5", "INVALID_FOOD_RATING");
        }
        FoodFeedback feedback = foodFeedbackRepository
                .findByTenantProfileAndMenuDateAndMealType(tenant, request.menuDate(), request.mealType())
                .orElseGet(FoodFeedback::new);
        feedback.setTenantProfile(tenant);
        feedback.setProperty(tenant.getProperty());
        feedback.setMenuDate(request.menuDate());
        feedback.setMealType(request.mealType());
        feedback.setRating(request.rating());
        feedback.setComment(blankToNull(request.comment()));
        FoodFeedback saved = foodFeedbackRepository.save(feedback);
        auditService.log(actor, "FOOD_FEEDBACK_SUBMITTED", "OPERATIONS", "FoodFeedback", saved.getId(),
                "Food feedback submitted", null, saved.getMealType().name(), ipAddress);
        return mapper.toFoodFeedback(saved);
    }

    @Transactional(readOnly = true)
    public List<FoodFeedbackResponse> listFeedbackForOwner(Long ownerUserId, Long propertyId) {
        OwnerProfile owner = accessService.owner(ownerUserId);
        List<FoodFeedback> feedback = propertyId == null
                ? foodFeedbackRepository.findAllByOwner(owner)
                : foodFeedbackRepository.findAllByPropertyAndOwner(accessService.ownedProperty(propertyId, owner), owner);
        return feedback.stream().map(mapper::toFoodFeedback).toList();
    }

    private FoodMenu ownerMenu(OwnerProfile owner, Long menuId) {
        return foodMenuRepository.findByIdAndOwner(menuId, owner)
                .orElseThrow(() -> {
                    if (menuId != null && foodMenuRepository.existsById(menuId)) {
                        return new ApiException(HttpStatus.FORBIDDEN, "Food menu access denied", "FOOD_MENU_ACCESS_DENIED");
                    }
                    return new ApiException(HttpStatus.NOT_FOUND, "Food menu not found", "FOOD_MENU_NOT_FOUND");
                });
    }

    private void applyMenu(FoodMenu menu, PgProperty property, FoodMenuRequest request) {
        menu.setProperty(property);
        menu.setMenuDate(request.menuDate());
        menu.setMealType(request.mealType());
        menu.setItems(request.items().trim());
        menu.setNotes(blankToNull(request.notes()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
