package com.makemytrip.makemytrip.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;
@Document(collection = "users")
public class Users {
    @Id
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String role;
    @org.springframework.data.annotation.Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String adminSignupToken;
    private String phoneNumber;
    private List<Booking> bookings = new ArrayList<>();;


    public String getFirstName() {return firstName;}
    public String getId() {
        return _id;
    }
    public void setId(String id){this._id=id;}
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getPassword() {return password;}
    public String getEmail() {return email;}
    public String getRole() {return role;}
    public String getAdminSignupToken() { return adminSignupToken; }
    public void setAdminSignupToken(String adminSignupToken) { this.adminSignupToken = adminSignupToken; }
    public void setPassword(String password) {this.password = password;}
    public void setEmail(String email) {this.email=email;}
    public void setRole(String role) {this.role = role;}
    public List<Booking> getBookings(){return bookings;}
    public void setBookings(List<Booking> bookings){this.bookings=bookings;}


    public static class Booking{
        private String bookingDocumentId;
        private String status;
        private String type;
        private String bookingId;
        private String date;
        private int quantity;
        private double totalPrice;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        public String getBookingDocumentId(){return bookingDocumentId;}
        public void setBookingDocumentId(String bookingDocumentId){this.bookingDocumentId=bookingDocumentId;}
        public String getStatus(){return status;}
        public void setStatus(String status){this.status=status;}

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
