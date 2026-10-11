package com.makemytrip.makemytrip.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.services.BookingService;
import com.makemytrip.makemytrip.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired private UserRepository userRepository;

    private void requireOwnUser(String userId, Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var authenticatedUser = userRepository.findByEmail(authentication.getName());
        if (authenticatedUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!authenticatedUser.getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot book for another user");
    }

    @PostMapping("/flight")
    public com.makemytrip.makemytrip.models.Booking bookFlight(@RequestParam String userId, @RequestParam String flightId, @RequestParam int seats, @RequestParam(defaultValue="0") double price, @RequestParam(required=false) String paymentReference, Authentication authentication) {
        requireOwnUser(userId, authentication);
        return bookingService.bookFlight(userId, flightId, seats, price, paymentReference);
    }

    @PostMapping("/hotel")
    public com.makemytrip.makemytrip.models.Booking bookhotel(@RequestParam String userId, @RequestParam String hotelId, @RequestParam int rooms, @RequestParam(defaultValue="0") double price, @RequestParam(required=false) String paymentReference, Authentication authentication) {
        requireOwnUser(userId, authentication);
        return bookingService.bookhotel(userId, hotelId, rooms, price, paymentReference);
    }

    @PostMapping("/service")
    public com.makemytrip.makemytrip.models.Booking bookService(@RequestParam String userId, @RequestParam String serviceOptionId, @RequestParam int quantity, @RequestParam(defaultValue="0") double price, @RequestParam(required=false) String paymentReference, Authentication authentication) {
        requireOwnUser(userId, authentication);
        return bookingService.bookTravelOption(userId, serviceOptionId, quantity, price, paymentReference);
    }
}
