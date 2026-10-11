package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "travel_options")
public class TravelOption {
    public enum Type { BUS, CAB, TRAIN, HOMESTAY, INSURANCE, HOLIDAY }
    @Id private String id;
    private Type type;
    private String name;
    private String provider;
    private String from;
    private String to;
    private String location;
    private String departureTime;
    private String arrivalTime;
    private double price;
    private int availableUnits;
    private String description;
    private List<String> amenities;
    private List<String> images;

    public String getId(){return id;} public void setId(String v){id=v;}
    public Type getType(){return type;} public void setType(Type v){type=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
    public String getFrom(){return from;} public void setFrom(String v){from=v;}
    public String getTo(){return to;} public void setTo(String v){to=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getDepartureTime(){return departureTime;} public void setDepartureTime(String v){departureTime=v;}
    public String getArrivalTime(){return arrivalTime;} public void setArrivalTime(String v){arrivalTime=v;}
    public double getPrice(){return price;} public void setPrice(double v){price=v;}
    public int getAvailableUnits(){return availableUnits;} public void setAvailableUnits(int v){availableUnits=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public List<String> getAmenities(){return amenities;} public void setAmenities(List<String> v){amenities=v;}
    public List<String> getImages(){return images;} public void setImages(List<String> v){images=v;}
}
