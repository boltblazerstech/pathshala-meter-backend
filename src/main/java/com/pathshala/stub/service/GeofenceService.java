package com.pathshala.stub.service;

import com.pathshala.stub.entity.GeofenceEvent;
import com.pathshala.stub.entity.LocationPoint;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.entity.UserGeofenceState;
import com.pathshala.stub.repository.GeofenceEventRepository;
import com.pathshala.stub.repository.UserGeofenceStateRepository;
import com.pathshala.stub.util.GeoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class GeofenceService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UserGeofenceStateRepository geofenceStateRepository;
    private final GeofenceEventRepository geofenceEventRepository;
    private final TrackingWindowService trackingWindowService;

    public GeofenceService(UserGeofenceStateRepository geofenceStateRepository,
                           GeofenceEventRepository geofenceEventRepository,
                           TrackingWindowService trackingWindowService) {
        this.geofenceStateRepository = geofenceStateRepository;
        this.geofenceEventRepository = geofenceEventRepository;
        this.trackingWindowService = trackingWindowService;
    }

    @Transactional
    public void processLocationPoints(User user, List<LocationPoint> points) {
        if (points == null || points.isEmpty()) return;

        int radiusLeaveHome = trackingWindowService.getConfigInt("radius_leave_home_meters", 100);
        int radiusReachSchool = trackingWindowService.getConfigInt("radius_reach_school_meters", 100);
        int radiusLeaveSchool = trackingWindowService.getConfigInt("radius_leave_school_meters", 100);
        int radiusReachHome = trackingWindowService.getConfigInt("radius_reach_home_meters", 100);

        UserGeofenceState state = geofenceStateRepository.findById(user.getId())
                .orElseGet(() -> new UserGeofenceState(user.getId()));

        for (LocationPoint point : points) {
            LocalDate pointDate = point.getCapturedAt().atZone(IST).toLocalDate();

            if (state.getStateDate() == null || !state.getStateDate().equals(pointDate)) {
                state.setStateDate(pointDate);
                state.setAtHome(true);
                state.setAtSchool(false);
            }

            Paathshaala targetPaathshaala = trackingWindowService.resolveTargetPaathshaalaForTime(user, point.getCapturedAt());
            boolean stateChanged = false;

            if (user.getHomeLat() != null && user.getHomeLng() != null) {
                double distanceToHome = GeoUtils.haversineMeters(
                        point.getLat(), point.getLng(),
                        user.getHomeLat(), user.getHomeLng()
                );

                if (state.isAtHome() && distanceToHome > radiusLeaveHome) {
                    recordEvent(user.getId(), "LEFT_HOME", point, null, pointDate);
                    state.setAtHome(false);
                    stateChanged = true;
                } else if (!state.isAtHome() && distanceToHome <= radiusReachHome) {
                    recordEvent(user.getId(), "REACHED_HOME", point, null, pointDate);
                    state.setAtHome(true);
                    stateChanged = true;
                }
            }

            if (!stateChanged && targetPaathshaala != null && targetPaathshaala.getLatitude() != null && targetPaathshaala.getLongitude() != null) {
                double distanceToSchool = GeoUtils.haversineMeters(
                        point.getLat(), point.getLng(),
                        targetPaathshaala.getLatitude(), targetPaathshaala.getLongitude()
                );

                if (!state.isAtSchool() && distanceToSchool <= radiusReachSchool) {
                    recordEvent(user.getId(), "REACHED_SCHOOL", point, targetPaathshaala.getId(), pointDate);
                    state.setAtSchool(true);
                } else if (state.isAtSchool() && distanceToSchool > radiusLeaveSchool) {
                    recordEvent(user.getId(), "LEFT_SCHOOL", point, targetPaathshaala.getId(), pointDate);
                    state.setAtSchool(false);
                }
            }

            state.setLastUpdated(point.getCapturedAt());
        }

        geofenceStateRepository.save(state);
    }

    private void recordEvent(UUID userId, String eventType, LocationPoint point, UUID paathshaalaId, LocalDate date) {
        GeofenceEvent event = new GeofenceEvent(
                userId,
                eventType,
                point.getCapturedAt(),
                date,
                point.getLat(),
                point.getLng(),
                paathshaalaId
        );
        geofenceEventRepository.save(event);
    }
}
