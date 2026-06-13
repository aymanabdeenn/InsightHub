package com.ayman.paymentservice.payment;

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

}
