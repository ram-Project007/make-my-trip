package com.makemytrip.makemytrip.services;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.TravelOption;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import com.makemytrip.makemytrip.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private MongoTemplate mongoTemplate;
    @Autowired private PricingEngine pricingEngine;
    @Autowired private LegacyBookingMirrorService legacyBookingMirror;

    public com.makemytrip.makemytrip.models.Booking bookFlight(String userId,String flightId,int seats,double price,String paymentReference){
        if (seats <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Number of seats must be greater than zero");
        if (paymentReference != null && !paymentReference.isBlank() && (paymentReference.trim().length() < 6 || paymentReference.trim().length() > 100)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid payment reference");
        if (!Double.isFinite(price) || price < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid quoted price");
        Optional<Users> usersOptional =userRepository.findById(userId);
        Users user=usersOptional.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        var pending=bookingRepository.findFirstByUserIdAndFlightIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(userId,flightId,com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING,com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION);
        if(pending.isPresent())return pending.get();
        double total=Math.round(pricingEngine.currentFlight(flightId).getCurrentPrice()*seats*100d)/100d;
        Query query=new Query(Criteria.where("_id").is(flightId).and("availableSeats").gte(seats));
        Flight reserved=mongoTemplate.findAndModify(query,new Update().inc("availableSeats",-seats),FindAndModifyOptions.options().returnNew(true),Flight.class);
        if(reserved==null){if(!flightRepository.existsById(flightId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Flight not found");throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough seats available");}
        com.makemytrip.makemytrip.models.Booking document=new com.makemytrip.makemytrip.models.Booking(); document.setUserId(userId); document.setBookingType(com.makemytrip.makemytrip.models.Booking.Type.FLIGHT); document.setFlightId(flightId); document.setQuantity(seats); document.setTotalPrice(total); document.setStatus(com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING); document.setPaymentStatus(com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION); document.setPaymentReference(paymentReference==null||paymentReference.isBlank()?null:paymentReference.trim());
        try{document=bookingRepository.save(document);}catch(RuntimeException ex){mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(flightId)),new Update().inc("availableSeats",seats),Flight.class);throw ex;}
        try{legacyBookingMirror.sync(document);}catch(RuntimeException ex){bookingRepository.deleteById(document.getId());mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(flightId)),new Update().inc("availableSeats",seats),Flight.class);throw ex;}
        return document;
    }
    public com.makemytrip.makemytrip.models.Booking bookhotel(String userId,String hotelId,int rooms,double price,String paymentReference){
        if (rooms <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Number of rooms must be greater than zero");
        if (paymentReference != null && !paymentReference.isBlank() && (paymentReference.trim().length() < 6 || paymentReference.trim().length() > 100)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid payment reference");
        if (!Double.isFinite(price) || price < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid quoted price");
        Optional<Users> usersOptional =userRepository.findById(userId);
        Users user=usersOptional.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        var pending=bookingRepository.findFirstByUserIdAndHotelIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(userId,hotelId,com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING,com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION);
        if(pending.isPresent())return pending.get();
        double total=Math.round(pricingEngine.currentHotel(hotelId).getCurrentPrice()*rooms*100d)/100d;

        Query query=new Query(Criteria.where("_id").is(hotelId).and("availableRooms").gte(rooms));
        Hotel reserved=mongoTemplate.findAndModify(query,new Update().inc("availableRooms",-rooms),FindAndModifyOptions.options().returnNew(true),Hotel.class);
        if(reserved==null){if(!hotelRepository.existsById(hotelId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hotel not found");throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough rooms available");}
        com.makemytrip.makemytrip.models.Booking document=new com.makemytrip.makemytrip.models.Booking(); document.setUserId(userId); document.setBookingType(com.makemytrip.makemytrip.models.Booking.Type.HOTEL); document.setHotelId(hotelId); document.setQuantity(rooms); document.setTotalPrice(total); document.setStatus(com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING); document.setPaymentStatus(com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION); document.setPaymentReference(paymentReference==null||paymentReference.isBlank()?null:paymentReference.trim());
        try{document=bookingRepository.save(document);}catch(RuntimeException ex){mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(hotelId)),new Update().inc("availableRooms",rooms),Hotel.class);throw ex;}
        try{legacyBookingMirror.sync(document);}catch(RuntimeException ex){bookingRepository.deleteById(document.getId());mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(hotelId)),new Update().inc("availableRooms",rooms),Hotel.class);throw ex;}
        return document;
    }

    public com.makemytrip.makemytrip.models.Booking bookTravelOption(String userId,String optionId,int quantity,double quotedPrice,String paymentReference){
        if(quantity<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be greater than zero");
        if(paymentReference!=null&&!paymentReference.isBlank()&&(paymentReference.trim().length()<6||paymentReference.trim().length()>100))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid payment reference");
        if(!Double.isFinite(quotedPrice)||quotedPrice<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid quoted price");
        userRepository.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        var pending=bookingRepository.findFirstByUserIdAndServiceOptionIdAndStatusAndPaymentStatusOrderByCreatedAtDesc(userId,optionId,com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING,com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION);
        if(pending.isPresent())return pending.get();

        TravelOption option=mongoTemplate.findAndModify(new Query(Criteria.where("_id").is(optionId).and("availableUnits").gte(quantity)),new Update().inc("availableUnits",-quantity),FindAndModifyOptions.options().returnNew(true),TravelOption.class);
        if(option==null){if(!mongoTemplate.exists(new Query(Criteria.where("_id").is(optionId)),TravelOption.class))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Travel option not found");throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough availability");}
        double total=Math.round(option.getPrice()*quantity*100d)/100d;
        com.makemytrip.makemytrip.models.Booking document=new com.makemytrip.makemytrip.models.Booking();
        document.setUserId(userId);document.setBookingType(com.makemytrip.makemytrip.models.Booking.Type.valueOf(option.getType().name()));document.setServiceOptionId(optionId);document.setQuantity(quantity);document.setTotalPrice(total);document.setStatus(com.makemytrip.makemytrip.models.Booking.Status.PAYMENT_PENDING);document.setPaymentStatus(com.makemytrip.makemytrip.models.Booking.PaymentStatus.PENDING_VERIFICATION);document.setPaymentReference(paymentReference==null||paymentReference.isBlank()?null:paymentReference.trim());
        try{document=bookingRepository.save(document);legacyBookingMirror.sync(document);return document;}
        catch(RuntimeException ex){if(document.getId()!=null)bookingRepository.deleteById(document.getId());mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(optionId)),new Update().inc("availableUnits",quantity),TravelOption.class);throw ex;}
    }

}
