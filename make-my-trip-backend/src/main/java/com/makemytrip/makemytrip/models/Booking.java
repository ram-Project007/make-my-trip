package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.time.LocalDate;

@Document("bookings")
public class Booking {
    public enum Type { FLIGHT, HOTEL, BUS, CAB, TRAIN, HOMESTAY, INSURANCE, HOLIDAY }
    public enum Status { PAYMENT_PENDING, PAYMENT_REJECTED, CONFIRMED, CANCELLED, COMPLETED }
    public enum PaymentStatus { NOT_REQUIRED, PENDING_VERIFICATION, PAID, REJECTED }
    @Id private String id;
    @Indexed private String userId;
    @Indexed private Type bookingType;
    @Indexed private Status status = Status.CONFIRMED;
    @Indexed private String flightId;
    @Indexed private String hotelId;
    @Indexed private String serviceOptionId;
    private LocalDate bookingDate = LocalDate.now();
    private LocalDate travelDate;
    private LocalDate checkInDate;
    private int quantity;
    private double totalPrice;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private String seatNumber;
    private String roomType;
    private String roomId;
    private double selectionSurcharge;
    private PaymentStatus paymentStatus = PaymentStatus.NOT_REQUIRED;
    private String paymentReference;
    private String paytmOrderId;
    private String paytmQrImage;
    private Instant paytmQrExpiresAt;

    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public Type getBookingType(){return bookingType;} public void setBookingType(Type v){bookingType=v;}
    public Status getStatus(){return status;} public void setStatus(Status v){status=v;touch();}
    public String getFlightId(){return flightId;} public void setFlightId(String v){flightId=v;}
    public String getHotelId(){return hotelId;} public void setHotelId(String v){hotelId=v;}
    public String getServiceOptionId(){return serviceOptionId;} public void setServiceOptionId(String v){serviceOptionId=v;}
    public LocalDate getBookingDate(){return bookingDate;} public void setBookingDate(LocalDate v){bookingDate=v;}
    public LocalDate getTravelDate(){return travelDate;} public void setTravelDate(LocalDate v){travelDate=v;}
    public LocalDate getCheckInDate(){return checkInDate;} public void setCheckInDate(LocalDate v){checkInDate=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public double getTotalPrice(){return totalPrice;} public void setTotalPrice(double v){totalPrice=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
    public String getSeatNumber(){return seatNumber;} public void setSeatNumber(String v){seatNumber=v;}
    public String getRoomType(){return roomType;} public void setRoomType(String v){roomType=v;}
    public String getRoomId(){return roomId;} public void setRoomId(String v){roomId=v;}
    public double getSelectionSurcharge(){return selectionSurcharge;} public void setSelectionSurcharge(double v){selectionSurcharge=v;}
    public PaymentStatus getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(PaymentStatus v){paymentStatus=v;touch();}
    public String getPaymentReference(){return paymentReference;} public void setPaymentReference(String v){paymentReference=v;}
    public String getPaytmOrderId(){return paytmOrderId;} public void setPaytmOrderId(String v){paytmOrderId=v;touch();}
    public String getPaytmQrImage(){return paytmQrImage;} public void setPaytmQrImage(String v){paytmQrImage=v;touch();}
    public Instant getPaytmQrExpiresAt(){return paytmQrExpiresAt;} public void setPaytmQrExpiresAt(Instant v){paytmQrExpiresAt=v;touch();}
    public String getProductId(){if(bookingType==Type.FLIGHT)return flightId;if(bookingType==Type.HOTEL)return hotelId;return serviceOptionId;}
    public void setProductId(String v){if(bookingType==Type.FLIGHT)flightId=v;else if(bookingType==Type.HOTEL)hotelId=v;else serviceOptionId=v;}
    public String getType(){return bookingType==null?null:bookingType.name();}
    public void setType(String v){bookingType=Type.valueOf(v.toUpperCase());}
    public void touch(){updatedAt=Instant.now();}
}
