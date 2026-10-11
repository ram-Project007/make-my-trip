package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.models.CancellationReason;
import com.makemytrip.makemytrip.models.Refund;
import com.makemytrip.makemytrip.repositories.BookingRepository;
import com.makemytrip.makemytrip.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
public class CancellationService {
    private final BookingRepository bookings;
    private final RefundRepository refunds;
    private final MongoTemplate mongo;
    private final LegacyBookingMirrorService legacyMirror;
    @Value("${travel.refund.within-hours:24}") private long refundWindowHours;
    @Value("${travel.refund.percentage:50}") private double refundPercentage;

    public CancellationService(BookingRepository bookings, RefundRepository refunds, MongoTemplate mongo) {
        this(bookings,refunds,mongo,null);
    }
    @Autowired public CancellationService(BookingRepository bookings,RefundRepository refunds,MongoTemplate mongo,LegacyBookingMirrorService legacyMirror){this.bookings=bookings;this.refunds=refunds;this.mongo=mongo;this.legacyMirror=legacyMirror;}

    public Refund cancel(String bookingId, String userId, CancellationReason reason) {
        if (reason == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid cancellation reason is required");
        Booking booking=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));
        if (!booking.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Booking belongs to another user");
        if (booking.getStatus()!=Booking.Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking is not cancellable");
        Instant now=Instant.now();
        Query q=new Query(Criteria.where("_id").is(bookingId).and("userId").is(userId).and("status").is(Booking.Status.CONFIRMED));
        Update u=new Update().set("status",Booking.Status.CANCELLED).set("updatedAt",now);
        if (mongo.updateFirst(q,u,Booking.class).getModifiedCount()!=1) throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking is not cancellable");
        booking.setStatus(Booking.Status.CANCELLED);booking.setUpdatedAt(now);
        if(booking.getBookingType()==Booking.Type.FLIGHT){mongo.updateFirst(new Query(Criteria.where("_id").is(booking.getFlightId())),new Update().inc("availableSeats",booking.getQuantity()),com.makemytrip.makemytrip.models.Flight.class);if(booking.getSeatNumber()!=null){Query seat=new Query(Criteria.where("flightId").is(booking.getFlightId()).and("seatNumber").is(booking.getSeatNumber()).and("bookingId").is(bookingId));mongo.updateFirst(seat,new Update().set("status",com.makemytrip.makemytrip.models.Seat.Status.AVAILABLE).unset("bookingId"),com.makemytrip.makemytrip.models.Seat.class);}}
        if(booking.getBookingType()==Booking.Type.HOTEL){mongo.updateFirst(new Query(Criteria.where("_id").is(booking.getHotelId())),new Update().inc("availableRooms",booking.getQuantity()),com.makemytrip.makemytrip.models.Hotel.class);if(booking.getRoomId()!=null){Query room=new Query(Criteria.where("_id").is(booking.getRoomId()));mongo.updateFirst(room,new Update().inc("availableQuantity",booking.getQuantity()),com.makemytrip.makemytrip.models.Room.class);}}
        if(booking.getBookingType()!=Booking.Type.FLIGHT&&booking.getBookingType()!=Booking.Type.HOTEL){mongo.updateFirst(new Query(Criteria.where("_id").is(booking.getServiceOptionId())),new Update().inc("availableUnits",booking.getQuantity()),com.makemytrip.makemytrip.models.TravelOption.class);}
        Refund refund=new Refund(); refund.setBookingId(bookingId); refund.setUserId(userId);
        refund.setOriginalAmount(booking.getTotalPrice()); refund.setCancellationReason(reason);
        Duration elapsed=Duration.between(booking.getCreatedAt(),now);
        double pct=!elapsed.isNegative()&&elapsed.compareTo(Duration.ofHours(refundWindowHours))<=0?refundPercentage:0d;
        refund.setRefundPercentage(pct); refund.setRefundAmount(Math.round(booking.getTotalPrice()*pct)/100d);
        refund.setExpectedCompletionDate(now.plus(Duration.ofDays(7)));
        Refund saved=refunds.save(refund);
        if(legacyMirror!=null)legacyMirror.sync(booking);
        return saved;
    }
}
