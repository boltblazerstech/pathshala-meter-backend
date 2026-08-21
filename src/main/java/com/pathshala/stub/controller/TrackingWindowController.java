package com.pathshala.stub.controller;

import com.pathshala.stub.dto.TrackingWindowResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TrackingWindowController {

    @Value("${tracking.default-start:09:00}")
    private String defaultStart;

    @Value("${tracking.default-end:17:00}")
    private String defaultEnd;

    @Value("${tracking.default-interval:15}")
    private int defaultInterval;

    @GetMapping("/tracking-window")
    public TrackingWindowResponse getTrackingWindow(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Integer interval) {
        
        String startTime = start != null ? start : defaultStart;
        String endTime = end != null ? end : defaultEnd;
        int intervalMinutes = interval != null ? interval : defaultInterval;

        return new TrackingWindowResponse(startTime, endTime, intervalMinutes);
    }
}
