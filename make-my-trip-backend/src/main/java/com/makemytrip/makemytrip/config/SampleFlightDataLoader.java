package com.makemytrip.makemytrip.config;

import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SampleFlightDataLoader {

    @Bean
    CommandLineRunner loadSampleFlights(FlightRepository flightRepository) {
        return args -> {
            Set<String> existingFlightNumbers = flightRepository.findAll().stream()
                    .map(Flight::getFlightNumber)
                    .collect(Collectors.toSet());

            List<Flight> sampleFlights = List.of(
                    flight("IndiGo 6E-201", "Delhi", "Mumbai", "2026-11-01T06:15", "2026-11-01T08:25", 5499, 42, "6E-201", "IndiGo", "T1", "A12"),
                    flight("Air India AI-864", "Mumbai", "Delhi", "2026-11-01T07:00", "2026-11-01T09:10", 6299, 38, "AI-864", "Air India", "T2", "B04"),
                    flight("Akasa Air QP-1351", "Bengaluru", "Mumbai", "2026-11-01T08:20", "2026-11-01T10:05", 4799, 56, "QP-1351", "Akasa Air", "T1", "C08"),
                    flight("SpiceJet SG-8169", "Hyderabad", "Delhi", "2026-11-01T09:10", "2026-11-01T11:30", 5899, 31, "SG-8169", "SpiceJet", "T3", "D16"),
                    flight("IndiGo 6E-503", "Chennai", "Bengaluru", "2026-11-01T10:00", "2026-11-01T11:05", 3299, 47, "6E-503", "IndiGo", "T1", "A06"),
                    flight("Air India AI-542", "Delhi", "Kolkata", "2026-11-01T11:25", "2026-11-01T13:35", 6999, 29, "AI-542", "Air India", "T3", "B19"),
                    flight("Vistara UK-955", "Mumbai", "Bengaluru", "2026-11-01T12:10", "2026-11-01T13:55", 7199, 24, "UK-955", "Vistara", "T2", "E03"),
                    flight("IndiGo 6E-681", "Pune", "Delhi", "2026-11-01T13:00", "2026-11-01T15:10", 6199, 51, "6E-681", "IndiGo", "T1", "A21"),
                    flight("Akasa Air QP-1402", "Ahmedabad", "Mumbai", "2026-11-01T14:15", "2026-11-01T15:25", 3899, 44, "QP-1402", "Akasa Air", "T1", "C11"),
                    flight("Air India AI-839", "Kochi", "Bengaluru", "2026-11-01T15:30", "2026-11-01T16:40", 4299, 36, "AI-839", "Air India", "T1", "B08"),
                    flight("IndiGo 6E-2124", "Jaipur", "Mumbai", "2026-11-01T16:05", "2026-11-01T18:00", 5199, 58, "6E-2124", "IndiGo", "T2", "A14"),
                    flight("SpiceJet SG-297", "Goa", "Delhi", "2026-11-01T17:20", "2026-11-01T20:00", 7499, 27, "SG-297", "SpiceJet", "T1", "D05"),
                    flight("Vistara UK-817", "Delhi", "Hyderabad", "2026-11-01T18:00", "2026-11-01T20:10", 6899, 33, "UK-817", "Vistara", "T3", "E11"),
                    flight("IndiGo 6E-623", "Kolkata", "Guwahati", "2026-11-01T19:15", "2026-11-01T20:35", 4599, 49, "6E-623", "IndiGo", "T2", "A09"),
                    flight("Air India AI-567", "Bengaluru", "Chennai", "2026-11-01T20:10", "2026-11-01T21:10", 3599, 41, "AI-567", "Air India", "T1", "B12"),
                    flight("Akasa Air QP-1320", "Mumbai", "Ahmedabad", "2026-11-02T06:40", "2026-11-02T07:50", 3799, 53, "QP-1320", "Akasa Air", "T1", "C04"),
                    flight("IndiGo 6E-742", "Delhi", "Lucknow", "2026-11-02T08:05", "2026-11-02T09:15", 3999, 46, "6E-742", "IndiGo", "T2", "A18"),
                    flight("Air India AI-951", "Hyderabad", "Mumbai", "2026-11-02T10:30", "2026-11-02T12:00", 5699, 35, "AI-951", "Air India", "T1", "B07"),
                    flight("Vistara UK-747", "Mumbai", "Kolkata", "2026-11-02T12:45", "2026-11-02T15:20", 7799, 22, "UK-747", "Vistara", "T2", "E15"),
                    flight("IndiGo 6E-318", "Bengaluru", "Delhi", "2026-11-02T16:20", "2026-11-02T19:05", 8299, 39, "6E-318", "IndiGo", "T1", "A03")
            );

            List<Flight> missingFlights = sampleFlights.stream()
                    .filter(sample -> !existingFlightNumbers.contains(sample.getFlightNumber()))
                    .toList();
            if (!missingFlights.isEmpty()) {
                flightRepository.saveAll(missingFlights);
            }
        };
    }

    private Flight flight(String name, String from, String to, String departure, String arrival,
                          double price, int seats, String number, String airline, String terminal, String gate) {
        Flight flight = new Flight();
        flight.setFlightName(name);
        flight.setFrom(from);
        flight.setTo(to);
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight.setPrice(price);
        flight.setAvailableSeats(seats);
        flight.setFlightNumber(number);
        flight.setAirline(airline);
        flight.setTerminal(terminal);
        flight.setGate(gate);
        return flight;
    }
}
