package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.FlightNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface FlightNotificationRepository extends MongoRepository<FlightNotification,String>{List<FlightNotification> findByUserIdOrderByCreatedAtDesc(String userId);Optional<FlightNotification> findByIdAndUserId(String id,String userId);}
