package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.RoomPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
public interface RoomPreferenceRepository extends MongoRepository<RoomPreference,String>{Optional<RoomPreference> findByUserId(String userId);void deleteByUserId(String userId);}
