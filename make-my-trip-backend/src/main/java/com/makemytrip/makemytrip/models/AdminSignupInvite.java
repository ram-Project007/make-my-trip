package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "admin_signup_invites")
public class AdminSignupInvite {
    @Id private String tokenHash;
    private Instant usedAt;
    private String usedBy;
    public AdminSignupInvite() {}
    public AdminSignupInvite(String tokenHash) { this.tokenHash = tokenHash; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public String getUsedBy() { return usedBy; }
    public void setUsedBy(String usedBy) { this.usedBy = usedBy; }
}
