package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document("room_preferences")
public class RoomPreference {
    @Id private String id;
    @Indexed(unique=true) private String userId;
    private String preferredRoomType;
    private Integer minimumCapacity;
    private List<String> preferredAmenities;
    private Boolean preferPremium;
    private Instant createdAt=Instant.now();
    private Instant updatedAt=Instant.now();
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getPreferredRoomType(){return preferredRoomType;} public void setPreferredRoomType(String v){preferredRoomType=v;}
    public Integer getMinimumCapacity(){return minimumCapacity;} public void setMinimumCapacity(Integer v){minimumCapacity=v;}
    public List<String> getPreferredAmenities(){return preferredAmenities;} public void setPreferredAmenities(List<String> v){preferredAmenities=v;}
    public Boolean getPreferPremium(){return preferPremium;} public void setPreferPremium(Boolean v){preferPremium=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
