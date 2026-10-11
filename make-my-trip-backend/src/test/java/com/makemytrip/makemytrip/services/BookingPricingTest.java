package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.*;
import com.makemytrip.makemytrip.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingPricingTest {
    @Test void clientSuppliedPriceDoesNotControlStoredFlightBookingTotal() {
        UserRepository users=mock(UserRepository.class); FlightRepository flights=mock(FlightRepository.class);
        HotelRepository hotels=mock(HotelRepository.class); BookingRepository bookings=mock(BookingRepository.class);
        MongoTemplate mongo=mock(MongoTemplate.class); PricingEngine pricing=mock(PricingEngine.class);
        Users user=new Users();user.setEmail("traveler@example.com");when(users.findById("u1")).thenReturn(Optional.of(user));
        Flight flight=new Flight();flight.setAvailableSeats(8);
        when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Flight.class))).thenReturn(flight);
        PriceHistory quote=new PriceHistory();quote.setCurrentPrice(12500d);when(pricing.currentFlight("f1")).thenReturn(quote);
        when(bookings.save(any(com.makemytrip.makemytrip.models.Booking.class))).thenAnswer(invocation->invocation.getArgument(0));
        BookingService service=new BookingService();
        org.springframework.test.util.ReflectionTestUtils.setField(service,"userRepository",users);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"flightRepository",flights);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"hotelRepository",hotels);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"bookingRepository",bookings);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"mongoTemplate",mongo);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"pricingEngine",pricing);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"legacyBookingMirror",mock(LegacyBookingMirrorService.class));

        com.makemytrip.makemytrip.models.Booking saved=service.bookFlight("u1","f1",2,1d,"TXN123456");
        assertEquals(25000d,saved.getTotalPrice());
        assertEquals(Booking.Status.PAYMENT_PENDING,saved.getStatus());
        assertEquals(Booking.PaymentStatus.PENDING_VERIFICATION,saved.getPaymentStatus());
        verify(bookings).save(argThat(b->b.getTotalPrice()==25000d));
    }
}
