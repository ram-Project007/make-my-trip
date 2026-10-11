package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("review_helpful_votes")
@CompoundIndex(name = "review_helpful_user_unique", def = "{'reviewId':1,'userId':1}", unique = true)
public class ReviewHelpfulVote {
    @Id private String id;
    private String reviewId;
    private String userId;
    private Instant createdAt = Instant.now();
    public String getId(){return id;}
    public String getReviewId(){return reviewId;}
    public void setReviewId(String value){reviewId=value;}
    public String getUserId(){return userId;}
    public void setUserId(String value){userId=value;}
    public Instant getCreatedAt(){return createdAt;}
}
