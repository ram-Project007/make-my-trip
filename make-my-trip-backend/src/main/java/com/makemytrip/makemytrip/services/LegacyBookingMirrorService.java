package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.Objects;

/** Keeps the old embedded booking response shape synchronized for existing clients. */
@Service
public class LegacyBookingMirrorService {
    private final UserRepository users;
    public LegacyBookingMirrorService(UserRepository users){this.users=users;}
    public void sync(Booking source){
        Users user=users.findById(source.getUserId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(user.getBookings()==null)user.setBookings(new ArrayList<>());
        String product=source.getProductId();
        String type=source.getType();
        Users.Booking target=user.getBookings().stream().filter(x->Objects.equals(source.getId(),x.getBookingDocumentId())).findFirst().orElseGet(()->user.getBookings().stream().filter(x->x.getBookingDocumentId()==null&&Objects.equals(product,x.getBookingId())&&type.equalsIgnoreCase(x.getType())&&source.getBookingDate()!=null&&source.getBookingDate().toString().equals(x.getDate())).findFirst().orElseGet(()->{Users.Booking created=new Users.Booking();user.getBookings().add(created);return created;}));
        target.setBookingDocumentId(source.getId());target.setType(type);target.setBookingId(product);target.setDate(source.getBookingDate()==null?null:source.getBookingDate().toString());target.setQuantity(source.getQuantity());target.setTotalPrice(source.getTotalPrice());target.setStatus(source.getStatus()==null?null:source.getStatus().name());
        users.save(user);
    }
}
