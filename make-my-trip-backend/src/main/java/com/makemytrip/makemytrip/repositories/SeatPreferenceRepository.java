package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.SeatPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
public interface SeatPreferenceRepository extends MongoRepository<SeatPreference,String>{Optional<SeatPreference> findByUserId(String userId);void deleteByUserId(String userId);}
