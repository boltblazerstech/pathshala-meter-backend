package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.repository.PaathshalaRepository;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaathshalaService {

    private final PaathshalaRepository paathshalaRepository;
    private final UserRepository       userRepository;
    private final MapLinkParser        mapLinkParser;

    public PaathshalaService(PaathshalaRepository paathshalaRepository,
                             UserRepository userRepository,
                             MapLinkParser mapLinkParser) {
        this.paathshalaRepository = paathshalaRepository;
        this.userRepository       = userRepository;
        this.mapLinkParser        = mapLinkParser;
    }

    @Transactional
    public PaathshalaResponse create(CreatePaathshalaRequest request) {
        ParsedCoordinate coord = mapLinkParser.parseMapLink(request.mapLink());

        Paathshaala entity = new Paathshaala();
        entity.setName(request.name());
        entity.setLatitude(coord.lat());
        entity.setLongitude(coord.lng());
        entity.setSourceMapLink(request.mapLink());
        entity.setCoordinateConfidence(coord.confidence());

        return toResponse(paathshalaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaathshalaResponse> findAll(Pageable pageable) {
        Page<PaathshalaResponse> page = paathshalaRepository
                .findAll(pageable)
                .map(this::toResponse);
        return PagedResponse.of(page);
    }

    @Transactional
    public PaathshalaResponse update(UUID id, UpdatePaathshalaRequest request) {
        Paathshaala entity = paathshalaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Paathshaala not found: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name());
        }

        if (request.mapLink() != null && !request.mapLink().isBlank()) {
            // Re-parse coordinates from the new link
            ParsedCoordinate coord = mapLinkParser.parseMapLink(request.mapLink());
            entity.setLatitude(coord.lat());
            entity.setLongitude(coord.lng());
            entity.setSourceMapLink(request.mapLink());
            entity.setCoordinateConfidence(coord.confidence());
        }

        return toResponse(paathshalaRepository.save(entity));
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

    // ── Mapping ──────────────────────────────────────────────────────
    private PaathshalaResponse toResponse(Paathshaala p) {
        return new PaathshalaResponse(
                p.getId(),
                p.getName(),
                p.getLatitude(),
                p.getLongitude(),
                p.getSourceMapLink(),
                p.getCoordinateConfidence(),
                p.getCreatedAt()
        );
    }
}
