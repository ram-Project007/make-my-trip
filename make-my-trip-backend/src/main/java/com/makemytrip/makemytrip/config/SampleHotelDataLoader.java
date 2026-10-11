package com.makemytrip.makemytrip.config;

import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SampleHotelDataLoader {

    private static final List<String> SAMPLE_HOTEL_IMAGES = List.of(
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1578774204375-826dc5d996ed?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"
    );

    @Bean
    CommandLineRunner loadSampleHotels(HotelRepository hotelRepository) {
        return args -> {
            Set<String> existingHotelNames = hotelRepository.findAll().stream()
                    .map(Hotel::gethotelName)
                    .collect(Collectors.toSet());

            List<Hotel> sampleHotels = List.of(
                    hotel("The Imperial New Delhi", "New Delhi", 14500, 18, "Wi-Fi, pool, spa, restaurant, airport shuttle"),
                    hotel("Taj Palace", "New Delhi", 12800, 22, "Wi-Fi, pool, spa, fitness center, restaurant"),
                    hotel("The Leela Palace Bengaluru", "Bengaluru", 16200, 14, "Wi-Fi, pool, spa, garden, restaurant"),
                    hotel("ITC Gardenia", "Bengaluru", 9800, 25, "Wi-Fi, pool, fitness center, restaurant, parking"),
                    hotel("Taj Mahal Palace", "Mumbai", 18500, 12, "Wi-Fi, pool, spa, sea view, restaurant"),
                    hotel("Trident Nariman Point", "Mumbai", 11900, 20, "Wi-Fi, pool, sea view, fitness center, restaurant"),
                    hotel("The Oberoi Mumbai", "Mumbai", 17300, 10, "Wi-Fi, pool, spa, sea view, airport shuttle"),
                    hotel("ITC Maratha", "Mumbai", 8900, 28, "Wi-Fi, pool, spa, restaurant, airport shuttle"),
                    hotel("Taj Coromandel", "Chennai", 10200, 19, "Wi-Fi, pool, fitness center, restaurant, parking"),
                    hotel("The Leela Palace Chennai", "Chennai", 13700, 13, "Wi-Fi, pool, spa, sea view, restaurant"),
                    hotel("Taj Krishna", "Hyderabad", 9400, 24, "Wi-Fi, pool, garden, restaurant, fitness center"),
                    hotel("ITC Kohenur", "Hyderabad", 11100, 17, "Wi-Fi, pool, spa, lake view, restaurant"),
                    hotel("Taj Bengal", "Kolkata", 8700, 21, "Wi-Fi, pool, garden, spa, restaurant"),
                    hotel("The Oberoi Grand", "Kolkata", 9200, 16, "Wi-Fi, pool, heritage building, restaurant, fitness center"),
                    hotel("Taj Fort Aguada Resort", "Goa", 15600, 11, "Wi-Fi, pool, beach access, spa, restaurant"),
                    hotel("Grand Hyatt Goa", "Goa", 12400, 23, "Wi-Fi, pool, beach access, kids club, restaurant"),
                    hotel("Taj Resort & Convention Centre Goa", "Goa", 10800, 15, "Wi-Fi, pool, sea view, spa, restaurant"),
                    hotel("Taj Lake Palace", "Udaipur", 22400, 7, "Wi-Fi, pool, lake view, boat transfer, spa"),
                    hotel("The Oberoi Udaivilas", "Udaipur", 24800, 8, "Wi-Fi, pool, lake view, spa, restaurant"),
                    hotel("Jaipur Marriott Hotel", "Jaipur", 7600, 27, "Wi-Fi, pool, spa, fitness center, restaurant")
            );

            List<Hotel> missingHotels = sampleHotels.stream()
                    .filter(sample -> !existingHotelNames.contains(sample.gethotelName()))
                    .toList();
            if (!missingHotels.isEmpty()) {
                hotelRepository.saveAll(missingHotels);
            }
        };
    }

    private Hotel hotel(String name, String location, double pricePerNight, int availableRooms, String amenities) {
        Hotel hotel = new Hotel();
        hotel.sethotelName(name);
        hotel.setLocation(location);
        hotel.setPricePerNight(pricePerNight);
        hotel.setAvailableRooms(availableRooms);
        hotel.setamenities(amenities);
        int imageIndex = Math.floorMod(name.hashCode(), SAMPLE_HOTEL_IMAGES.size());
        hotel.setImages(List.of(
                SAMPLE_HOTEL_IMAGES.get(imageIndex),
                SAMPLE_HOTEL_IMAGES.get((imageIndex + 1) % SAMPLE_HOTEL_IMAGES.size()),
                SAMPLE_HOTEL_IMAGES.get((imageIndex + 2) % SAMPLE_HOTEL_IMAGES.size())
        ));
        return hotel;
    }
}
