package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.TravelOption;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface TravelOptionRepository extends MongoRepository<TravelOption,String> {
    List<TravelOption> findByType(TravelOption.Type type);
}
