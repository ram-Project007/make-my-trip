package com.makemytrip.makemytrip.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "flight")
public class Flight {
    @Id
    private String _id;
    private String flightName;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private double price;
    private int availableSeats;
    private String flightNumber;
    private String airline;
    private String terminal;
    private String gate;

    // Getters and Setters

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getFlightName() {
        return flightName;
    }

    public void setFlightName(String flightName) {
        this.flightName = flightName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    public String getFlightNumber(){return flightNumber==null?flightName:flightNumber;}
    public void setFlightNumber(String flightNumber){this.flightNumber=flightNumber;}
    public String getAirline(){return airline;}
    public void setAirline(String airline){this.airline=airline;}
    public String getTerminal(){return terminal;}
    public void setTerminal(String terminal){this.terminal=terminal;}
    public String getGate(){return gate;}
    public void setGate(String gate){this.gate=gate;}
}
