package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface BookingRepository extends MongoRepository<Booking,String> {
 List<Booking> findByUserId(String userId);
 Optional<Booking> findFirstByUserIdAndFlightIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(String userId,String flightId,Booking.Status status,Booking.PaymentStatus paymentStatus);
 Optional<Booking> findFirstByUserIdAndHotelIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(String userId,String hotelId,Booking.Status status,Booking.PaymentStatus paymentStatus);
 Optional<Booking> findFirstByUserIdAndServiceOptionIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(String userId,String serviceOptionId,Booking.Status status,Booking.PaymentStatus paymentStatus);
 List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);
 List<Booking> findByUserIdAndStatus(String userId, Booking.Status status);
 List<Booking> findByFlightId(String flightId);
 List<Booking> findByHotelId(String hotelId);
 boolean existsByUserIdAndFlightIdAndBookingDate(String userId,String flightId,java.time.LocalDate bookingDate);
 boolean existsByUserIdAndHotelIdAndBookingDate(String userId,String hotelId,java.time.LocalDate bookingDate);
 long countByFlightIdAndBookingDateAfter(String flightId,java.time.LocalDate since);
 long countByHotelIdAndBookingDateAfter(String hotelId,java.time.LocalDate since);
}
