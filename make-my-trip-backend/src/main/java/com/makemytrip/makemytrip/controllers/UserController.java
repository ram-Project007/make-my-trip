package com.makemytrip.makemytrip.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.makemytrip.dto.LoginRequest;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.services.UserServices;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import com.makemytrip.makemytrip.repositories.UserRepository;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserServices userServices;
    @Autowired private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Users> login(@RequestBody LoginRequest request){
        Users user = userServices.login(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }
    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody Users user){
        return ResponseEntity.ok(userServices.signup(user));
    }
    @GetMapping("/email")
    public ResponseEntity<Users> getuserbyemail(@RequestParam String email,Authentication authentication){
        if(authentication==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if(!authentication.getName().equalsIgnoreCase(email))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cannot access another user's profile");
        Users user = userServices.getUserByEmail(authentication.getName());
        if(user != null){
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/edit")
    public ResponseEntity<Users> editprofile(@RequestParam String id ,@RequestBody Users updatedUser,Authentication authentication){
        if(authentication==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Users current=userRepository.findByEmail(authentication.getName());
        if(current==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if(!current.getId().equals(id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cannot edit another user's profile");
        Users user = userServices.editprofile(id,updatedUser);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
