package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.models.CancellationReason;
import com.makemytrip.makemytrip.models.Refund;
import com.makemytrip.makemytrip.repositories.BookingRepository;
import com.makemytrip.makemytrip.repositories.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {
    @Mock BookingRepository bookings;
    @Mock RefundRepository refunds;
    @Mock MongoTemplate mongo;
    @InjectMocks CancellationService service;
    Booking booking;

    @BeforeEach void setup(){ReflectionTestUtils.setField(service,"refundWindowHours",24L);ReflectionTestUtils.setField(service,"refundPercentage",50d);booking=new Booking();booking.setId("b1");booking.setUserId("u1");booking.setBookingType(Booking.Type.FLIGHT);booking.setFlightId("f1");booking.setTotalPrice(200);booking.setCreatedAt(Instant.now().minusSeconds(3600));}

    @Test void cancelsOwnConfirmedBookingAndCalculatesHalfRefund(){when(bookings.findById("b1")).thenReturn(Optional.of(booking));when(mongo.updateFirst(any(Query.class),any(Update.class),eq(Booking.class))).thenReturn(UpdateResult.acknowledged(1,1L,null));when(refunds.save(any(Refund.class))).thenAnswer(i->i.getArgument(0));Refund r=service.cancel("b1","u1",CancellationReason.CHANGE_OF_PLANS);assertEquals(100,r.getRefundAmount());assertEquals(50,r.getRefundPercentage());assertEquals(Refund.Status.PENDING,r.getRefundStatus());}
    @Test void cancellationAfterWindowHasZeroRefund(){booking.setCreatedAt(Instant.now().minusSeconds(26*3600));when(bookings.findById("b1")).thenReturn(Optional.of(booking));when(mongo.updateFirst(any(Query.class),any(Update.class),eq(Booking.class))).thenReturn(UpdateResult.acknowledged(1,1L,null));when(refunds.save(any(Refund.class))).thenAnswer(i->i.getArgument(0));assertEquals(0,service.cancel("b1","u1",CancellationReason.OTHER).getRefundAmount());}
    @Test void rejectsMissingBooking(){when(bookings.findById("missing")).thenReturn(Optional.empty());ResponseStatusException e=assertThrows(ResponseStatusException.class,()->service.cancel("missing","u1",CancellationReason.OTHER));assertEquals(HttpStatus.NOT_FOUND,e.getStatusCode());}
    @Test void rejectsOtherUsersBooking(){when(bookings.findById("b1")).thenReturn(Optional.of(booking));ResponseStatusException e=assertThrows(ResponseStatusException.class,()->service.cancel("b1","u2",CancellationReason.OTHER));assertEquals(HttpStatus.FORBIDDEN,e.getStatusCode());verifyNoInteractions(mongo,refunds);}
    @Test void preventsDuplicateConcurrentCancellation(){when(bookings.findById("b1")).thenReturn(Optional.of(booking));when(mongo.updateFirst(any(Query.class),any(Update.class),eq(Booking.class))).thenReturn(UpdateResult.acknowledged(0,0L,null));ResponseStatusException e=assertThrows(ResponseStatusException.class,()->service.cancel("b1","u1",CancellationReason.OTHER));assertEquals(HttpStatus.CONFLICT,e.getStatusCode());verifyNoInteractions(refunds);}
}
