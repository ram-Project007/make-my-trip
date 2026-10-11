package com.makemytrip.makemytrip.controllers;

import com.makemytrip.makemytrip.models.TravelOption;
import com.makemytrip.makemytrip.repositories.TravelOptionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/travel/options")
@CrossOrigin(origins = "*")
public class TravelOptionController {
    private final TravelOptionRepository options;
    public TravelOptionController(TravelOptionRepository options){this.options=options;}

    @GetMapping
    public List<TravelOption> list(@RequestParam TravelOption.Type type){return options.findByType(type);}

    @GetMapping("/{id}")
    public TravelOption get(@PathVariable String id){return options.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Travel option not found"));}
}
