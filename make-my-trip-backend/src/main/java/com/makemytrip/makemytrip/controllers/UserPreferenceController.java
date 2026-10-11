package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.dto.RoomPreferenceRequest;
import com.makemytrip.makemytrip.dto.SeatPreferenceRequest;
import com.makemytrip.makemytrip.models.RoomPreference;
import com.makemytrip.makemytrip.models.SeatPreference;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.services.UserPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me/preferences")
public class UserPreferenceController {
    private final UserPreferenceService preferences;
    private final UserRepository users;
    public UserPreferenceController(UserPreferenceService preferences,UserRepository users){this.preferences=preferences;this.users=users;}
    private String userId(Authentication auth){if(auth==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);Users user=users.findByEmail(auth.getName());if(user==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);return user.getId();}
    @GetMapping("/seat") public SeatPreference getSeat(Authentication auth){return preferences.seat(userId(auth));}
    @PutMapping("/seat") public SeatPreference saveSeat(@Valid @RequestBody SeatPreferenceRequest request,Authentication auth){return preferences.saveSeat(userId(auth),request);}
    @DeleteMapping("/seat") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteSeat(Authentication auth){preferences.deleteSeat(userId(auth));}
    @GetMapping("/room") public RoomPreference getRoom(Authentication auth){return preferences.room(userId(auth));}
    @PutMapping("/room") public RoomPreference saveRoom(@Valid @RequestBody RoomPreferenceRequest request,Authentication auth){return preferences.saveRoom(userId(auth),request);}
    @DeleteMapping("/room") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteRoom(Authentication auth){preferences.deleteRoom(userId(auth));}
}
