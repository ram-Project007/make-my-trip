package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.*;
import com.makemytrip.makemytrip.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceFreezeServiceTest {
 @Test void freezeUsesServerStoredBookingPriceAndOwner(){BookingRepository bookings=mock(BookingRepository.class);PriceFreezeRepository freezes=mock(PriceFreezeRepository.class);PriceFreezeService service=new PriceFreezeService(bookings,freezes,mock(MongoTemplate.class));Booking b=new Booking();b.setUserId("u1");b.setTotalPrice(950);when(bookings.findById("b1")).thenReturn(Optional.of(b));when(freezes.save(any())).thenAnswer(i->i.getArgument(0));PriceFreeze freeze=service.create("b1","u1");assertEquals(950,freeze.getFrozenPrice());assertEquals(PriceFreeze.Status.ACTIVE,freeze.getStatus());assertThrows(ResponseStatusException.class,()->service.create("b1","u2"));}
 @Test void expiredFreezeCannotBeUsed(){BookingRepository bookings=mock(BookingRepository.class);PriceFreezeRepository freezes=mock(PriceFreezeRepository.class);MongoTemplate mongo=mock(MongoTemplate.class);PriceFreezeService service=new PriceFreezeService(bookings,freezes,mongo);PriceFreeze freeze=new PriceFreeze();freeze.setUserId("u1");freeze.setExpiresAt(Instant.now().minusSeconds(1));when(freezes.findById("p1")).thenReturn(Optional.of(freeze));when(freezes.save(any())).thenAnswer(i->i.getArgument(0));assertThrows(ResponseStatusException.class,()->service.use("p1","u1"));assertEquals(PriceFreeze.Status.EXPIRED,freeze.getStatus());}
 @Test void activeFreezeIsAtomicallyMarkedUsed(){BookingRepository bookings=mock(BookingRepository.class);PriceFreezeRepository freezes=mock(PriceFreezeRepository.class);MongoTemplate mongo=mock(MongoTemplate.class);PriceFreeze used=new PriceFreeze();used.setUserId("u1");used.setStatus(PriceFreeze.Status.USED);when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(PriceFreeze.class))).thenReturn(used);PriceFreeze result=new PriceFreezeService(bookings,freezes,mongo).use("p1","u1");assertEquals(PriceFreeze.Status.USED,result.getStatus());verifyNoInteractions(freezes);}
}
