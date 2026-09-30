package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.repository.PaathshalaRepository;
import com.pathshala.stub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaathshalaService {

    private static final Logger log = LoggerFactory.getLogger(PaathshalaService.class);

    private final PaathshalaRepository paathshalaRepository;
    private final UserRepository       userRepository;
    private final MapLinkParser        mapLinkParser;
    private final GeocodeService       geocodeService;

    public PaathshalaService(PaathshalaRepository paathshalaRepository,
                             UserRepository userRepository,
                             MapLinkParser mapLinkParser,
                             GeocodeService geocodeService) {
        this.paathshalaRepository = paathshalaRepository;
        this.userRepository       = userRepository;
        this.mapLinkParser        = mapLinkParser;
        this.geocodeService       = geocodeService;
    }

    @Transactional
    public PaathshalaResponse create(CreatePaathshalaRequest request) {
        Paathshaala entity = new Paathshaala();
        entity.setName(request.name());

        processSupervisor(entity, request.supervisorId());
        processTimes(entity, request.openingTime(), request.closingTime());

        // Priority 1: Manual lat/lng provided directly
        if (request.lat() != null && request.lng() != null) {
            entity.setLatitude(request.lat());
            entity.setLongitude(request.lng());
            entity.setCoordinateConfidence("manual");
            // Still store the map link if provided, for reference
            if (request.mapLink() != null && !request.mapLink().isBlank()) {
                entity.setSourceMapLink(request.mapLink());
            }
        }
        // Priority 2: Parse from map link
        else if (request.mapLink() != null && !request.mapLink().isBlank()) {
            entity.setSourceMapLink(request.mapLink());
            try {
                ParsedCoordinate coord = mapLinkParser.parseMapLink(request.mapLink());
                entity.setLatitude(coord.lat());
                entity.setLongitude(coord.lng());
                entity.setCoordinateConfidence(coord.confidence());
            } catch (Exception e) {
                log.warn("Failed to parse map link for new paathshaala '{}': {}",
                        request.name(), e.getMessage());
                entity.setLatitude(null);
                entity.setLongitude(null);
                entity.setCoordinateConfidence("unresolved");
            }
        }
        // Priority 3: Neither provided
        else {
            entity.setLatitude(null);
            entity.setLongitude(null);
            entity.setCoordinateConfidence("unresolved");
        }

        resolveAddress(entity);

        return toResponse(paathshalaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaathshalaResponse> findAll(Pageable pageable) {
        Page<Paathshaala> page = paathshalaRepository.findAll(pageable);
        
        java.util.Set<UUID> supervisorIds = page.getContent().stream()
                .map(Paathshaala::getSupervisorId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
                
        java.util.Map<UUID, String> supervisorNames = userRepository.findAllById(supervisorIds).stream()
                .collect(java.util.stream.Collectors.toMap(com.pathshala.stub.entity.User::getId, com.pathshala.stub.entity.User::getName));

        Page<PaathshalaResponse> responsePage = page.map(p -> toResponse(p, supervisorNames.get(p.getSupervisorId())));
        return PagedResponse.of(responsePage);
    }

    @Transactional
    public PaathshalaResponse update(UUID id, UpdatePaathshalaRequest request) {
        Paathshaala entity = paathshalaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Paathshaala not found: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name());
        }

        processSupervisor(entity, request.supervisorId());
        processTimes(entity, request.openingTime(), request.closingTime());

        boolean locationChanged = false;

        // Priority 1: Manual lat/lng takes precedence
        if (request.lat() != null && request.lng() != null) {
            entity.setLatitude(request.lat());
            entity.setLongitude(request.lng());
            entity.setCoordinateConfidence("manual");
            locationChanged = true;
            // Still update the map link if provided, for reference
            if (request.mapLink() != null && !request.mapLink().isBlank()) {
                entity.setSourceMapLink(request.mapLink());
            }
        }
        // Priority 2: Re-parse from new map link (no manual lat/lng)
        else if (request.mapLink() != null && !request.mapLink().isBlank()) {
            entity.setSourceMapLink(request.mapLink());
            try {
                ParsedCoordinate coord = mapLinkParser.parseMapLink(request.mapLink());
                entity.setLatitude(coord.lat());
                entity.setLongitude(coord.lng());
                entity.setCoordinateConfidence(coord.confidence());
                locationChanged = true;
            } catch (Exception e) {
                log.warn("Failed to parse updated map link for paathshaala '{}': {}",
                        entity.getName(), e.getMessage());
                // Keep existing lat/lng unchanged — only mark confidence as stale
                entity.setCoordinateConfidence("unresolved");
            }
        }
        
        if (locationChanged) {
            resolveAddress(entity);
        }

        Paathshaala saved = paathshalaRepository.save(entity);
        String supervisorName = null;
        if (saved.getSupervisorId() != null) {
            supervisorName = userRepository.findById(saved.getSupervisorId()).map(com.pathshala.stub.entity.User::getName).orElse(null);
        }
        return toResponse(saved, supervisorName);
    }

    @Transactional
    public void delete(UUID id) {
        if (!paathshalaRepository.existsById(id)) {
            throw new NoSuchElementException("Paathshaala not found: " + id);
        }

        long assignedCount = userRepository.countByAssignedPaathshalaId(id);
        if (assignedCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete paathshaala — " + assignedCount + " user(s) are still assigned to it. "
                            + "Reassign or deactivate them first.");
        }

        paathshalaRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<PaathshalaResponse> findBySupervisorId(UUID supervisorId) {
        if (!userRepository.existsById(supervisorId)) {
            throw new NoSuchElementException("Supervisor not found: " + supervisorId);
        }
        String supervisorName = userRepository.findById(supervisorId).map(com.pathshala.stub.entity.User::getName).orElse(null);
        return paathshalaRepository.findBySupervisorId(supervisorId).stream()
                .map(p -> toResponse(p, supervisorName))
                .collect(java.util.stream.Collectors.toList());
    }
    
    private void processSupervisor(Paathshaala entity, UUID supervisorId) {
        if (supervisorId == null) {
            entity.setSupervisorId(null);
            return;
        }
        com.pathshala.stub.entity.User user = userRepository.findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found: " + supervisorId));
        if (!"supervisor".equals(user.getRole())) {
            throw new IllegalArgumentException("User is not a supervisor: " + supervisorId);
        }
        entity.setSupervisorId(supervisorId);
    }

    private void processTimes(Paathshaala entity, String openingTime, String closingTime) {
        if ((openingTime == null && closingTime != null) || (openingTime != null && closingTime == null)) {
            throw new IllegalArgumentException("Both opening_time and closing_time must be provided together.");
        }
        if (openingTime == null) {
            entity.setOpeningTime(null);
            entity.setClosingTime(null);
        } else {
            entity.setOpeningTime(java.time.LocalTime.parse(openingTime));
            entity.setClosingTime(java.time.LocalTime.parse(closingTime));
        }
    }

    private void resolveAddress(Paathshaala entity) {
        if (entity.getLatitude() != null && entity.getLongitude() != null) {
            GeocodeResult geocodeResult = geocodeService.reverseGeocode(entity.getLatitude(), entity.getLongitude());
            if (geocodeResult.address() != null) {
                entity.setAddress(geocodeResult.address());
            }
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────
    private PaathshalaResponse toResponse(Paathshaala p) {
        String supervisorName = null;
        if (p.getSupervisorId() != null) {
            supervisorName = userRepository.findById(p.getSupervisorId()).map(com.pathshala.stub.entity.User::getName).orElse(null);
        }
        return toResponse(p, supervisorName);
    }

    private PaathshalaResponse toResponse(Paathshaala p, String supervisorName) {
        return new PaathshalaResponse(
                p.getId(),
                p.getName(),
                p.getLatitude(),
                p.getLongitude(),
                p.getAddress(),
                p.getSourceMapLink(),
                p.getCoordinateConfidence(),
                p.getCreatedAt(),
                p.getSupervisorId(),
                supervisorName,
                p.getOpeningTime() != null ? p.getOpeningTime().toString() : null,
                p.getClosingTime() != null ? p.getClosingTime().toString() : null
        );
    }
}
