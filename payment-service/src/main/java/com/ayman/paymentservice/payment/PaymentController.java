package com.ayman.paymentservice.payment;

import com.ayman.configlib.error.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @GetMapping
    public ResponseEntity<?> sendResponse() {
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/test-error")
    public ResponseEntity<?> triggerError() {
        throw new ApiException("INTERNAL_ERROR");
    }

}
