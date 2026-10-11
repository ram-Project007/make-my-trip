package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.services.PaytmDynamicQrService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/booking/{bookingId}/payment")
public class BookingPaymentController {
    private final PaytmDynamicQrService paytm;
    private final UserRepository users;

    public BookingPaymentController(PaytmDynamicQrService paytm, UserRepository users) {
        this.paytm = paytm;
        this.users = users;
    }

    @PostMapping("/qr")
    public PaytmDynamicQrService.QrResult createQr(@PathVariable String bookingId, Authentication authentication) {
        return paytm.createQr(bookingId, userId(authentication));
    }

    @PostMapping("/verify")
    public PaytmDynamicQrService.Verification verify(@PathVariable String bookingId, Authentication authentication) {
        return paytm.verify(bookingId, userId(authentication));
    }

    private String userId(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Users user = users.findByEmail(authentication.getName());
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return user.getId();
    }
}
