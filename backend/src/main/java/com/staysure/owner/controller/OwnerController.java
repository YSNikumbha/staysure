package com.staysure.owner.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.owner.dto.OwnerApplicationRequest;
import com.staysure.owner.dto.OwnerDashboardResponse;
import com.staysure.owner.dto.OwnerDocumentResponse;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.service.OwnerDocumentService;
import com.staysure.owner.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerDocumentService ownerDocumentService;

    public OwnerController(OwnerService ownerService, OwnerDocumentService ownerDocumentService) {
        this.ownerService = ownerService;
        this.ownerDocumentService = ownerDocumentService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> apply(@Valid @org.springframework.web.bind.annotation.RequestBody OwnerApplicationRequest request,
                                                                   HttpServletRequest servletRequest) {
        OwnerProfileResponse response = ownerService.apply(
                SecurityUtils.currentUserId(),
                request,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner application submitted", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Owner profile loaded", ownerService.getMyProfile(SecurityUtils.currentUserId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> updateMe(@Valid @org.springframework.web.bind.annotation.RequestBody OwnerApplicationRequest request,
                                                                      HttpServletRequest servletRequest) {
        OwnerProfileResponse response = ownerService.updateMyProfile(
                SecurityUtils.currentUserId(),
                request,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner profile updated", response));
    }

    @PreAuthorize("hasRole('PG_OWNER')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<OwnerDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success("Owner dashboard loaded", ownerService.getDashboard(SecurityUtils.currentUserId())));
    }

    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OwnerDocumentResponse>> uploadDocument(@RequestParam DocumentType documentType,
                                                                            @RequestParam(required = false) String documentNumber,
                                                                            @RequestPart("file") MultipartFile file,
                                                                            HttpServletRequest servletRequest) {
        OwnerDocumentResponse response = ownerDocumentService.upload(
                SecurityUtils.currentUserId(),
                documentType,
                documentNumber,
                file,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner document uploaded", response));
    }

    @GetMapping("/me/documents")
    public ResponseEntity<ApiResponse<List<OwnerDocumentResponse>>> listDocuments() {
        return ResponseEntity.ok(ApiResponse.success("Owner documents loaded",
                ownerDocumentService.listMine(SecurityUtils.currentUserId())));
    }

    @DeleteMapping("/me/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        ownerDocumentService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Owner document deleted"));
    }
}
