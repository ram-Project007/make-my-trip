package com.makemytrip.makemytrip.services;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.makemytrip.makemytrip.repositories.AdminSignupInviteRepository;
import com.makemytrip.makemytrip.models.AdminSignupInvite;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class UserServices{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired private MongoTemplate mongoTemplate;

    public Users login(String email ,String password){
        Users user = userRepository.findByEmail(email);
        if(user != null && passwordEncoder.matches(password,user.getPassword())){
            return  user;
        }
        return null;
    }

    public Users signup(Users user){
        if(userRepository.findByEmail(user.getEmail())!= null){
            throw new RuntimeException("Email is already registered");
        }
        user.setPassword(passwordEncoder.encode((user.getPassword())));
        // An admin role is granted only by consuming a one-time invite stored by its SHA-256 hash.
        String invite = user.getAdminSignupToken();
        if (invite != null && !invite.isBlank()) {
            String hash = sha256(invite.trim());
            Query query = Query.query(Criteria.where("_id").is(hash).and("usedAt").is(null));
            Update update = new Update().set("usedAt", Instant.now()).set("usedBy", user.getEmail());
            AdminSignupInvite consumed = mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), AdminSignupInvite.class);
            if (consumed == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or already used admin invite token");
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        // Identity and booking history are server-owned. Prevent signup JSON from
        // overwriting an existing Mongo document or seeding fake legacy bookings.
        user.setId(null);
        user.setBookings(new java.util.ArrayList<>());
        user.setAdminSignupToken(null);
        return userRepository.save(user);

    }

    private String sha256(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public Users getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Users editprofile(String id,Users updatedUser){
        Users user=userRepository.findById(id).orElse(null);
        if(user != null){
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            return userRepository.save(user);
        }
        return null;
    }


}
