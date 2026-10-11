package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.FlightNotification;
import com.makemytrip.makemytrip.models.FlightStatus;
import com.makemytrip.makemytrip.models.TrackedFlight;
import com.makemytrip.makemytrip.repositories.FlightNotificationRepository;
import com.makemytrip.makemytrip.repositories.TrackedFlightRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Service
public class FlightNotificationService {
    private final TrackedFlightRepository tracked;
    private final FlightNotificationRepository notifications;
    private final MongoTemplate mongo;
    public FlightNotificationService(TrackedFlightRepository tracked,FlightNotificationRepository notifications,MongoTemplate mongo){this.tracked=tracked;this.notifications=notifications;this.mongo=mongo;}

    public void statusChanged(FlightStatus before,FlightStatus after){
        if(before==null||after==null||before.getFlightId()==null||!Objects.equals(before.getFlightId(),after.getFlightId()))return;
        List<Event> events=new ArrayList<>();
        if(changed(before.getEstimatedDeparture(),after.getEstimatedDeparture()))events.add(new Event(FlightNotification.Type.DEPARTURE_TIME_CHANGED,"Departure time changed","The estimated departure time for flight "+after.getFlightNumber()+" has changed.",before.getEstimatedDeparture(),after.getEstimatedDeparture()));
        if(after.getStatus()==FlightStatus.State.DELAYED&&(before.getStatus()!=FlightStatus.State.DELAYED||after.getDelayMinutes()>before.getDelayMinutes()))events.add(new Event(FlightNotification.Type.DELAY,"Flight delayed","Flight "+after.getFlightNumber()+" is delayed by "+after.getDelayMinutes()+" minutes.",before.getDelayMinutes(),after.getDelayMinutes()));
        if(after.getStatus()==FlightStatus.State.BOARDING&&before.getStatus()!=FlightStatus.State.BOARDING)events.add(new Event(FlightNotification.Type.BOARDING,"Boarding started","Flight "+after.getFlightNumber()+" is now boarding.",before.getStatus(),after.getStatus()));
        if(changed(before.getEstimatedArrival(),after.getEstimatedArrival()))events.add(new Event(FlightNotification.Type.ESTIMATED_ARRIVAL_CHANGED,"Arrival time changed","The estimated arrival time for flight "+after.getFlightNumber()+" has changed.",before.getEstimatedArrival(),after.getEstimatedArrival()));
        if(after.getStatus()==FlightStatus.State.CANCELLED&&before.getStatus()!=FlightStatus.State.CANCELLED)events.add(new Event(FlightNotification.Type.CANCELLED,"Flight cancelled","Flight "+after.getFlightNumber()+" has been cancelled.",before.getStatus(),after.getStatus()));
        if(events.isEmpty())return;
        List<TrackedFlight> recipients=tracked.findByFlightId(after.getFlightId());
        for(TrackedFlight recipient:recipients)for(Event event:events){FlightNotification n=new FlightNotification();n.setUserId(recipient.getUserId());n.setFlightId(after.getFlightId());n.setType(event.type);n.setTitle(event.title);n.setMessage(event.message);n.setCreatedAt(Instant.now());Map<String,Object> metadata=new LinkedHashMap<>();metadata.put("flightNumber",after.getFlightNumber());metadata.put("previousValue",event.previous);metadata.put("currentValue",event.current);n.setMetadata(metadata);notifications.save(n);}
    }
    public List<FlightNotification> list(String user){return notifications.findByUserIdOrderByCreatedAtDesc(user);}
    public FlightNotification markRead(String id,String user){FlightNotification n=notifications.findByIdAndUserId(id,user).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(!n.isRead()){n.setRead(true);return notifications.save(n);}return n;}
    public void markAllRead(String user){mongo.updateMulti(new Query(Criteria.where("userId").is(user).and("read").is(false)),new Update().set("read",true),FlightNotification.class);}
    private boolean changed(String before,String after){return before!=null&&after!=null&&!before.equals(after);}
    private record Event(FlightNotification.Type type,String title,String message,Object previous,Object current){}
}
