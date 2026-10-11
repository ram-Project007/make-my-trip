package com.makemytrip.makemytrip.config;

import com.makemytrip.makemytrip.models.TravelOption;
import com.makemytrip.makemytrip.repositories.TravelOptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SampleTravelOptionDataLoader implements CommandLineRunner {
    private final TravelOptionRepository options;
    private final MongoTemplate mongo;
    private static final String QUANTITY_MIGRATION = "travel-option-samples-quantity-50-v1";
    public SampleTravelOptionDataLoader(TravelOptionRepository options, MongoTemplate mongo){this.options=options;this.mongo=mongo;}

    @Override public void run(String... args){
        List<TravelOption> data=List.of(
            option(TravelOption.Type.BUS,"Volvo AC Sleeper","RedBus Demo","Delhi","Jaipur",null,"2026-11-12T21:00:00","2026-11-13T03:30:00",899,50,"Overnight AC sleeper coach",List.of("AC","Sleeper","USB charging"),List.of("https://images.unsplash.com/photo-1570125909232-eb263c188f7e?auto=format&fit=crop&w=900")),
            option(TravelOption.Type.BUS,"Scania Multi-Axle","CityLink Demo","Mumbai","Pune",null,"2026-11-13T08:00:00","2026-11-13T12:00:00",599,50,"Comfortable intercity express bus",List.of("AC","Water","Recliner"),List.of("https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=900")),
            option(TravelOption.Type.TRAIN,"Deccan Express","Indian Rail Demo","Mumbai","Pune",null,"2026-11-14T07:00:00","2026-11-14T10:30:00",450,50,"Express train, chair car fare",List.of("Chair car","Pantry"),List.of()),
            option(TravelOption.Type.TRAIN,"Shatabdi Express","Indian Rail Demo","Delhi","Jaipur",null,"2026-11-15T06:00:00","2026-11-15T10:45:00",1250,50,"Fast intercity train, AC chair car",List.of("AC chair car","Meals"),List.of()),
            option(TravelOption.Type.CAB,"City Sedan","MakeMyTrip Cabs","Bengaluru","Mysuru",null,null,null,3200,50,"Private one-way sedan transfer",List.of("AC","4 seats","Driver included"),List.of("https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=900")),
            option(TravelOption.Type.CAB,"Airport SUV","MakeMyTrip Cabs","Delhi","Delhi Airport",null,null,null,1800,50,"Airport pickup in a spacious SUV",List.of("AC","6 seats","Luggage space"),List.of("https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=900")),
            option(TravelOption.Type.HOMESTAY,"Himalayan View Homestay","Local Host","Shimla","", "Shimla","", "", 2800,50,"Family-run stay with mountain views",List.of("Wi-Fi","Breakfast","Mountain view"),List.of("https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?auto=format&fit=crop&w=900")),
            option(TravelOption.Type.HOMESTAY,"Goa Garden Cottage","Local Host","","","Goa","","",3500,50,"Private cottage close to the beach",List.of("Wi-Fi","Kitchen","Garden"),List.of("https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=900"))
        );
        Query migrationQuery = new Query(Criteria.where("_id").is(QUANTITY_MIGRATION));
        if (!mongo.exists(migrationQuery, "sample_data_migrations")) {
            for (TravelOption sample : data) {
                mongo.updateFirst(new Query(Criteria.where("type").is(sample.getType()).and("name").is(sample.getName())),
                    new Update().inc("availableUnits", 30), TravelOption.class);
            }
            mongo.insert(new org.bson.Document("_id", QUANTITY_MIGRATION), "sample_data_migrations");
        }
        for(TravelOption.Type type:TravelOption.Type.values()){
            if(options.findByType(type).isEmpty())options.saveAll(data.stream().filter(option->option.getType()==type).toList());
        }
    }

    private TravelOption option(TravelOption.Type type,String name,String provider,String from,String to,String location,String departure,String arrival,double price,int units,String description,List<String> amenities,List<String> images){
        TravelOption o=new TravelOption();o.setType(type);o.setName(name);o.setProvider(provider);o.setFrom(from);o.setTo(to);o.setLocation(location);o.setDepartureTime(departure);o.setArrivalTime(arrival);o.setPrice(price);o.setAvailableUnits(units);o.setDescription(description);o.setAmenities(amenities);o.setImages(images);return o;
    }
}
