package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.*;
import com.makemytrip.makemytrip.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FlightNotificationServiceTest {
 @Test void importantTrackedFlightChangesCreateScopedEvents(){TrackedFlightRepository tracked=mock(TrackedFlightRepository.class);FlightNotificationRepository notifications=mock(FlightNotificationRepository.class);TrackedFlight watcher=new TrackedFlight();watcher.setUserId("u1");watcher.setFlightId("f1");when(tracked.findByFlightId("f1")).thenReturn(List.of(watcher));when(notifications.save(any())).thenAnswer(i->i.getArgument(0));FlightStatus before=new FlightStatus();before.setFlightId("f1");before.setFlightNumber("AB123");before.setStatus(FlightStatus.State.ON_TIME);before.setEstimatedDeparture("09:00");before.setEstimatedArrival("11:00");FlightStatus after=new FlightStatus();after.setFlightId("f1");after.setFlightNumber("AB123");after.setStatus(FlightStatus.State.CANCELLED);after.setEstimatedDeparture("10:00");after.setEstimatedArrival("12:00");new FlightNotificationService(tracked,notifications,mock(MongoTemplate.class)).statusChanged(before,after);verify(notifications,times(3)).save(argThat(n->"u1".equals(n.getUserId())&&"f1".equals(n.getFlightId())));}
}
