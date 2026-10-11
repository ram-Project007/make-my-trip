package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.services.BookingPaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/payments")
public class BookingPaymentAdminController {
    private final BookingPaymentService payments;

    public BookingPaymentAdminController(BookingPaymentService payments) {
        this.payments = payments;
    }

    @GetMapping("/pending")
    public List<Booking> pendingPayments() {
        return payments.pendingPayments();
    }

    @PutMapping("/{bookingId}/review")
    public Booking review(@PathVariable String bookingId, @RequestBody Map<String, String> request) {
        if (request == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "paymentStatus must be PAID or REJECTED");
        }
        Booking.PaymentStatus decision;
        try {
            decision = Booking.PaymentStatus.valueOf(String.valueOf(request.getOrDefault("paymentStatus", "")).trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "paymentStatus must be PAID or REJECTED");
        }
        return payments.review(bookingId, decision);
    }
}