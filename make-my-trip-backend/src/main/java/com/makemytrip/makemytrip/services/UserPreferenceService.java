package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.dto.RoomPreferenceRequest;
import com.makemytrip.makemytrip.dto.SeatPreferenceRequest;
import com.makemytrip.makemytrip.models.RoomPreference;
import com.makemytrip.makemytrip.models.SeatPreference;
import com.makemytrip.makemytrip.repositories.RoomPreferenceRepository;
import com.makemytrip.makemytrip.repositories.SeatPreferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@Service
public class UserPreferenceService {
    private final SeatPreferenceRepository seats;
    private final RoomPreferenceRepository rooms;
    public UserPreferenceService(SeatPreferenceRepository seats,RoomPreferenceRepository rooms){this.seats=seats;this.rooms=rooms;}

    public SeatPreference seat(String user){return seats.findByUserId(user).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Seat preference not found"));}
    public SeatPreference saveSeat(String user,SeatPreferenceRequest request){
        if(request.preferredClass()==null&&request.preferredType()==null&&request.preferWindow()==null&&request.preferAisle()==null&&request.preferredRow()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"At least one seat preference is required");
        if(Boolean.TRUE.equals(request.preferWindow())&&Boolean.TRUE.equals(request.preferAisle()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A seat cannot be both window and aisle preference");
        SeatPreference value=seats.findByUserId(user).orElseGet(SeatPreference::new);value.setUserId(user);value.setPreferredClass(request.preferredClass());value.setPreferredType(request.preferredType());value.setPreferWindow(request.preferWindow());value.setPreferAisle(request.preferAisle());value.setPreferredRow(request.preferredRow());value.setUpdatedAt(Instant.now());return seats.save(value);
    }
    public void deleteSeat(String user){seats.deleteByUserId(user);}
    public RoomPreference room(String user){return rooms.findByUserId(user).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Room preference not found"));}
    public RoomPreference saveRoom(String user,RoomPreferenceRequest request){if(request.minimumCapacity()!=null&&request.minimumCapacity()<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"minimumCapacity must be positive");RoomPreference value=rooms.findByUserId(user).orElseGet(RoomPreference::new);value.setUserId(user);value.setPreferredRoomType(request.preferredRoomType().trim());value.setMinimumCapacity(request.minimumCapacity());value.setPreferredAmenities(request.preferredAmenities()==null?null:request.preferredAmenities().stream().map(String::trim).distinct().toList());value.setPreferPremium(request.preferPremium());value.setUpdatedAt(Instant.now());return rooms.save(value);}
    public void deleteRoom(String user){rooms.deleteByUserId(user);}
}
