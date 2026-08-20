package com.staysure.rent.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.rent.dto.GenerateRentRequest;
import com.staysure.rent.dto.GenerateRentResponse;
import com.staysure.rent.dto.RecordRentPaymentRequest;
import com.staysure.rent.dto.RentDashboardResponse;
import com.staysure.rent.dto.RentInvoiceDetailResponse;
import com.staysure.rent.dto.RentPaymentResponse;
import com.staysure.rent.dto.UpdateRentChargesRequest;
import com.staysure.rent.service.RentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/rent")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerRentController {

    private final RentService rentService;

    public OwnerRentController(RentService rentService) {
        this.rentService = rentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RentDashboardResponse>> list(@RequestParam(required = false) Long propertyId) {
        return ResponseEntity.ok(ApiResponse.success("Rent invoices loaded",
                rentService.listForOwner(SecurityUtils.currentUserId(), propertyId)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateRentResponse>> generate(@Valid @RequestBody GenerateRentRequest request,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Rent generation completed",
                rentService.generate(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<RentInvoiceDetailResponse>> get(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success("Rent invoice loaded",
                rentService.getForOwner(SecurityUtils.currentUserId(), invoiceId)));
    }

    @PatchMapping("/{invoiceId}/charges")
    public ResponseEntity<ApiResponse<RentInvoiceDetailResponse>> updateCharges(@PathVariable Long invoiceId,
                                                                                @Valid @RequestBody UpdateRentChargesRequest request,
                                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Rent charges updated",
                rentService.updateCharges(SecurityUtils.currentUserId(), invoiceId, request,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @PostMapping("/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<RentInvoiceDetailResponse>> recordPayment(@PathVariable Long invoiceId,
                                                                                @Valid @RequestBody RecordRentPaymentRequest request,
                                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Rent payment recorded",
                rentService.recordPayment(SecurityUtils.currentUserId(), invoiceId, request,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<List<RentPaymentResponse>>> payments(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success("Rent payments loaded",
                rentService.paymentsForOwner(SecurityUtils.currentUserId(), invoiceId)));
    }

    @GetMapping("/payments/{paymentId}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long paymentId) {
        String receipt = rentService.receiptForOwner(SecurityUtils.currentUserId(), paymentId);
        return receiptResponse(receipt, paymentId);
    }

    private ResponseEntity<byte[]> receiptResponse(String receipt, Long paymentId) {
        byte[] body = receipt.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("rent-receipt-" + paymentId + ".txt")
                        .build()
                        .toString())
                .body(body);
    }
}
