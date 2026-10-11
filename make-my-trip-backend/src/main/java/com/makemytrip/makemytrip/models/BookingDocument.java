package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("bookings")
public class BookingDocument {
    @Id private String id;
    @Indexed private String userId;
    private String type;
    private String productId;
    private int quantity;
    private double totalPrice;
    private Instant createdAt = Instant.now();
    private String status = "CONFIRMED";
    private String seatNumber;
    private String roomType;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public String getProductId(){return productId;} public void setProductId(String v){productId=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public double getTotalPrice(){return totalPrice;} public void setTotalPrice(double v){totalPrice=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getSeatNumber(){return seatNumber;} public void setSeatNumber(String v){seatNumber=v;}
    public String getRoomType(){return roomType;} public void setRoomType(String v){roomType=v;}
}
