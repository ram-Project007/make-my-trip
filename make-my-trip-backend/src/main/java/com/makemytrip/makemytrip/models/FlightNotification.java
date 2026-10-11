package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Document("flight_notifications")
public class FlightNotification {
    public enum Type { DEPARTURE_TIME_CHANGED, DELAY, BOARDING, ESTIMATED_ARRIVAL_CHANGED, CANCELLED }
    @Id private String id;
    @Indexed private String userId;
    @Indexed private String flightId;
    private Type type;
    private String title;
    private String message;
    @Indexed private Instant createdAt=Instant.now();
    @Indexed private boolean read;
    private Map<String,Object> metadata;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getFlightId(){return flightId;} public void setFlightId(String v){flightId=v;}
    public Type getType(){return type;} public void setType(Type v){type=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public boolean isRead(){return read;} public void setRead(boolean v){read=v;}
    public Map<String,Object> getMetadata(){return metadata;} public void setMetadata(Map<String,Object> v){metadata=v;}
}
