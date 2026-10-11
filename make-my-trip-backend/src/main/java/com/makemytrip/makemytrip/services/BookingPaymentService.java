package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.TravelOption;
import com.makemytrip.makemytrip.repositories.BookingRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class BookingPaymentService {
    private final BookingRepository bookings;
    private final MongoTemplate mongo;
    private final LegacyBookingMirrorService legacyMirror;

    public BookingPaymentService(BookingRepository bookings, MongoTemplate mongo, LegacyBookingMirrorService legacyMirror) {
        this.bookings = bookings;
        this.mongo = mongo;
        this.legacyMirror = legacyMirror;
    }

    public List<Booking> pendingPayments() {
        return bookings.findByPaymentStatus(Booking.PaymentStatus.PENDING_VERIFICATION);
    }

    /** Applies a paid result only after the Paytm server-to-server status response is verified. */
    public Booking confirmProviderPayment(String bookingId, String transactionId) {
        Instant now = Instant.now();
        Query query = new Query(Criteria.where("_id").is(bookingId)
                .and("paymentStatus").is(Booking.PaymentStatus.PENDING_VERIFICATION)
                .and("status").is(Booking.Status.PAYMENT_PENDING));
        Update update = new Update()
                .set("paymentStatus", Booking.PaymentStatus.PAID)
                .set("status", Booking.Status.CONFIRMED)
                .set("paymentReference", transactionId)
                .set("updatedAt", now);
        Booking confirmed = mongo.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), Booking.class);
        if (confirmed == null) {
            Booking current = bookings.findById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
            if (current.getPaymentStatus() == Booking.PaymentStatus.PAID) return current;
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking payment is no longer pending");
        }
        legacyMirror.sync(confirmed);
        return confirmed;
    }

    public Booking review(String bookingId, Booking.PaymentStatus decision) {
        if (decision != Booking.PaymentStatus.PAID && decision != Booking.PaymentStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be PAID or REJECTED");
        }
        Booking existing = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        Instant now = Instant.now();
        Booking.Status nextStatus = decision == Booking.PaymentStatus.PAID
                ? Booking.Status.CONFIRMED
                : Booking.Status.PAYMENT_REJECTED;
        Query query = new Query(Criteria.where("_id").is(bookingId)
                .and("paymentStatus").is(Booking.PaymentStatus.PENDING_VERIFICATION));
        Update update = new Update()
                .set("paymentStatus", decision)
                .set("status", nextStatus)
                .set("updatedAt", now);
        Booking reviewed = mongo.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), Booking.class);
        if (reviewed == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment was already reviewed or is not pending");
        }

        if (decision == Booking.PaymentStatus.REJECTED && reviewed.getBookingType() == Booking.Type.FLIGHT) {
            mongo.updateFirst(new Query(Criteria.where("_id").is(reviewed.getFlightId())),
                    new Update().inc("availableSeats", reviewed.getQuantity()), Flight.class);
        } else if (decision == Booking.PaymentStatus.REJECTED && reviewed.getBookingType() == Booking.Type.HOTEL) {
            mongo.updateFirst(new Query(Criteria.where("_id").is(reviewed.getHotelId())),
                    new Update().inc("availableRooms", reviewed.getQuantity()), Hotel.class);
        } else if (decision == Booking.PaymentStatus.REJECTED) {
            mongo.updateFirst(new Query(Criteria.where("_id").is(reviewed.getServiceOptionId())),
                    new Update().inc("availableUnits", reviewed.getQuantity()), TravelOption.class);
        }
        legacyMirror.sync(reviewed);
        return reviewed;
    }
}
