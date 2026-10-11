package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("seat_preferences")
public class SeatPreference {
    @Id private String id;
    @Indexed(unique=true) private String userId;
    private Seat.SeatClass preferredClass;
    private Seat.SeatType preferredType;
    private Boolean preferWindow;
    private Boolean preferAisle;
    private Integer preferredRow;
    private Instant createdAt=Instant.now();
    private Instant updatedAt=Instant.now();
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public Seat.SeatClass getPreferredClass(){return preferredClass;} public void setPreferredClass(Seat.SeatClass v){preferredClass=v;}
    public Seat.SeatType getPreferredType(){return preferredType;} public void setPreferredType(Seat.SeatType v){preferredType=v;}
    public Boolean getPreferWindow(){return preferWindow;} public void setPreferWindow(Boolean v){preferWindow=v;}
    public Boolean getPreferAisle(){return preferAisle;} public void setPreferAisle(Boolean v){preferAisle=v;}
    public Integer getPreferredRow(){return preferredRow;} public void setPreferredRow(Integer v){preferredRow=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
