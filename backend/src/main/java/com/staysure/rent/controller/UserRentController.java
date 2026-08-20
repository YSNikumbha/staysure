package com.staysure.rent.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.rent.dto.RentDashboardResponse;
import com.staysure.rent.dto.RentInvoiceDetailResponse;
import com.staysure.rent.dto.RentPaymentResponse;
import com.staysure.rent.service.RentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/rent")
public class UserRentController {

    private final RentService rentService;

    public UserRentController(RentService rentService) {
        this.rentService = rentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RentDashboardResponse>> list() {
        return ResponseEntity.ok(ApiResponse.success("Rent invoices loaded",
                rentService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<RentInvoiceDetailResponse>> get(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success("Rent invoice loaded",
                rentService.getForUser(SecurityUtils.currentUserId(), invoiceId)));
    }

    @GetMapping("/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<List<RentPaymentResponse>>> payments(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success("Rent payments loaded",
                rentService.paymentsForUser(SecurityUtils.currentUserId(), invoiceId)));
    }

    @GetMapping("/payments/{paymentId}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long paymentId) {
        String receipt = rentService.receiptForUser(SecurityUtils.currentUserId(), paymentId);
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
