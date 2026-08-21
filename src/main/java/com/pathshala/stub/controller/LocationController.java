package com.pathshala.stub.controller;

import com.pathshala.stub.dto.BatchReceiveResponse;
import com.pathshala.stub.dto.LocationPointDto;
import com.pathshala.stub.entity.LocationPoint;
import com.pathshala.stub.repository.LocationPointRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationPointRepository repository;

    public LocationController(LocationPointRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/batch")
    public BatchReceiveResponse batchReceive(@RequestBody List<LocationPointDto> dtos) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        List<LocationPoint> points = dtos.stream().map(dto -> {
            LocationPoint point = new LocationPoint();
            point.setUserId(userId);
            point.setLat(dto.lat());
            point.setLng(dto.lng());
            point.setCapturedAt(Instant.parse(dto.capturedAt()));
            return point;
        }).collect(Collectors.toList());

        repository.saveAll(points);
        return new BatchReceiveResponse(points.size());
    }

    @GetMapping
    public List<LocationPoint> getLocations() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return repository.findByUserIdOrderByCapturedAtAsc(userId);
    }
}
