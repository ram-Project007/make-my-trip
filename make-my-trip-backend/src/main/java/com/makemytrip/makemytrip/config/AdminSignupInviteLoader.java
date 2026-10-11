package com.makemytrip.makemytrip.config;

import com.makemytrip.makemytrip.models.AdminSignupInvite;
import com.makemytrip.makemytrip.repositories.AdminSignupInviteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class AdminSignupInviteLoader implements CommandLineRunner {
    private final AdminSignupInviteRepository invites;
    private final String configuredTokens;

    public AdminSignupInviteLoader(AdminSignupInviteRepository invites,
            @Value("${admin.signup.tokens:}") String configuredTokens) {
        this.invites = invites;
        this.configuredTokens = configuredTokens;
    }

    @Override public void run(String... args) throws Exception {
        if (configuredTokens.isBlank()) return;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String token : configuredTokens.split(",")) {
            String value = token.trim();
            if (value.length() < 32) throw new IllegalStateException("Admin signup tokens must be at least 32 characters");
            String hash = HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
            if (!invites.existsById(hash)) invites.save(new AdminSignupInvite(hash));
        }
    }
}
