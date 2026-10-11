package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.models.TravelOption;
import com.makemytrip.makemytrip.repositories.TravelOptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/travel/options")
@CrossOrigin(origins = "*")
public class AdminTravelOptionController {
    private final TravelOptionRepository options;
    public AdminTravelOptionController(TravelOptionRepository options) { this.options = options; }

    @PostMapping
    public TravelOption create(@RequestBody TravelOption option) {
        validate(option);
        option.setId(null);
        return options.save(option);
    }

    @PutMapping("/{id}")
    public TravelOption update(@PathVariable String id, @RequestBody TravelOption updated) {
        TravelOption current = options.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel option not found"));
        validate(updated);
        updated.setId(current.getId());
        return options.save(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!options.existsById(id)) return ResponseEntity.notFound().build();
        options.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(TravelOption option) {
        if (option.getType() == null || option.getName() == null || option.getName().isBlank() || option.getPrice() < 0 || option.getAvailableUnits() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type, name, non-negative price and inventory are required");
        }
    }
}