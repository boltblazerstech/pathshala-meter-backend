package com.pathshala.stub.controller;

import com.pathshala.stub.dto.GeocodeResult;
import com.pathshala.stub.service.GeocodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/geocode")
public class AdminGeocodeController {

    private final GeocodeService geocodeService;

    public AdminGeocodeController(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

    @GetMapping("/reverse")
    public GeocodeResult reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        return geocodeService.reverseGeocode(lat, lng);
    }
}
