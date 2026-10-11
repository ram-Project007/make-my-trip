package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.models.FlightNotification;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.services.FlightNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/notifications")
public class FlightNotificationController {
    private final FlightNotificationService notifications;
    private final UserRepository users;
    public FlightNotificationController(FlightNotificationService notifications,UserRepository users){this.notifications=notifications;this.users=users;}
    private String userId(Authentication auth){if(auth==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);Users user=users.findByEmail(auth.getName());if(user==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);return user.getId();}
    @GetMapping public List<FlightNotification> list(Authentication auth){return notifications.list(userId(auth));}
    @PutMapping("/{id}/read") public FlightNotification read(@PathVariable String id,Authentication auth){return notifications.markRead(id,userId(auth));}
    @PutMapping("/read") @ResponseStatus(HttpStatus.NO_CONTENT) public void readAll(Authentication auth){notifications.markAllRead(userId(auth));}
}
