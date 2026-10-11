package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.dto.RoomPreferenceRequest;
import com.makemytrip.makemytrip.dto.SeatPreferenceRequest;
import com.makemytrip.makemytrip.models.*;
import com.makemytrip.makemytrip.repositories.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserPreferenceServiceTest {
 @Test void seatPreferenceIsScopedAndUpdatedForAuthenticatedOwner(){SeatPreferenceRepository seats=mock(SeatPreferenceRepository.class);RoomPreferenceRepository rooms=mock(RoomPreferenceRepository.class);UserPreferenceService service=new UserPreferenceService(seats,rooms);when(seats.findByUserId("user-a")).thenReturn(Optional.empty());when(seats.save(any())).thenAnswer(i->i.getArgument(0));SeatPreference saved=service.saveSeat("user-a",new SeatPreferenceRequest(Seat.SeatClass.ECONOMY,Seat.SeatType.WINDOW,true,false,12));assertEquals("user-a",saved.getUserId());assertEquals(Seat.SeatType.WINDOW,saved.getPreferredType());}
 @Test void roomPreferenceUsesServiceSuppliedOwnerRatherThanRequestData(){SeatPreferenceRepository seats=mock(SeatPreferenceRepository.class);RoomPreferenceRepository rooms=mock(RoomPreferenceRepository.class);UserPreferenceService service=new UserPreferenceService(seats,rooms);when(rooms.findByUserId("user-a")).thenReturn(Optional.empty());when(rooms.save(any())).thenAnswer(i->i.getArgument(0));RoomPreference saved=service.saveRoom("user-a",new RoomPreferenceRequest("DELUXE",2,java.util.List.of("WIFI"),true));assertEquals("user-a",saved.getUserId());assertEquals("DELUXE",saved.getPreferredRoomType());}
}
