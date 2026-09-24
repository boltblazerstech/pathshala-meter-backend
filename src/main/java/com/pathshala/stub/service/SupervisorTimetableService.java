package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.entity.SupervisorTimetable;
import com.pathshala.stub.entity.SupervisorTimetableOverride;
import com.pathshala.stub.repository.PaathshalaRepository;
import com.pathshala.stub.repository.SupervisorTimetableOverrideRepository;
import com.pathshala.stub.repository.SupervisorTimetableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupervisorTimetableService {

    private final SupervisorTimetableRepository timetableRepository;
    private final SupervisorTimetableOverrideRepository overrideRepository;
    private final PaathshalaRepository paathshalaRepository;

    public SupervisorTimetableService(SupervisorTimetableRepository timetableRepository,
                                      SupervisorTimetableOverrideRepository overrideRepository,
                                      PaathshalaRepository paathshalaRepository) {
        this.timetableRepository = timetableRepository;
        this.overrideRepository = overrideRepository;
        this.paathshalaRepository = paathshalaRepository;
    }

    @Transactional(readOnly = true)
    public TimetableResponse getTimetable(UUID supervisorId) {
        List<SupervisorTimetable> records = timetableRepository.findBySupervisorId(supervisorId);
        
        Map<String, SupervisorTimetable> recordMap = new HashMap<>();
        Set<UUID> paathshaalaIds = new HashSet<>();
        
        for (SupervisorTimetable t : records) {
            recordMap.put(t.getDayOfWeek() + "-" + t.getSlot(), t);
            if (t.getPaathshaalaId() != null) {
                paathshaalaIds.add(t.getPaathshaalaId());
            }
        }
        
        Map<UUID, String> pNames = paathshalaRepository.findAllById(paathshaalaIds).stream()
                .collect(Collectors.toMap(Paathshaala::getId, Paathshaala::getName));

        List<TimetableSlotDto> grid = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            for (String slot : List.of("MORNING", "EVENING")) {
                SupervisorTimetable t = recordMap.get(day + "-" + slot);
                if (t != null && t.getPaathshaalaId() != null) {
                    grid.add(new TimetableSlotDto(day, slot, t.getPaathshaalaId(), pNames.get(t.getPaathshaalaId())));
                } else {
                    grid.add(new TimetableSlotDto(day, slot, null, null));
                }
            }
        }
        return new TimetableResponse(grid);
    }

    @Transactional
    public TimetableResponse updateTimetable(UUID supervisorId, TimetableResponse request) {
        // Simple approach: clear existing, insert new
        List<SupervisorTimetable> existing = timetableRepository.findBySupervisorId(supervisorId);
        timetableRepository.deleteAll(existing);
        timetableRepository.flush();

        List<SupervisorTimetable> toSave = new ArrayList<>();
        for (TimetableSlotDto dto : request.slots()) {
            if (dto.paathshaalaId() != null) {
                toSave.add(new SupervisorTimetable(supervisorId, dto.dayOfWeek(), dto.slot(), dto.paathshaalaId()));
            }
        }
        timetableRepository.saveAll(toSave);
        return getTimetable(supervisorId);
    }

    @Transactional
    public TimetableOverrideDto setOverride(UUID supervisorId, TimetableOverrideDto request) {
        Optional<SupervisorTimetableOverride> existing = overrideRepository
                .findBySupervisorIdAndOverrideDateAndSlot(supervisorId, request.overrideDate(), request.slot());
        
        SupervisorTimetableOverride override;
        if (existing.isPresent()) {
            override = existing.get();
            override.setPaathshaalaId(request.paathshaalaId());
        } else {
            override = new SupervisorTimetableOverride(supervisorId, request.overrideDate(), request.slot(), request.paathshaalaId());
        }
        
        overrideRepository.save(override);
        
        String pName = null;
        if (request.paathshaalaId() != null) {
            pName = paathshalaRepository.findById(request.paathshaalaId()).map(Paathshaala::getName).orElse(null);
        }
        
        return new TimetableOverrideDto(override.getOverrideDate(), override.getSlot(), override.getPaathshaalaId(), pName);
    }

    @Transactional
    public void deleteOverride(UUID supervisorId, TimetableOverrideDeleteRequest request) {
        overrideRepository.findBySupervisorIdAndOverrideDateAndSlot(supervisorId, request.overrideDate(), request.slot())
                .ifPresent(overrideRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<TimetableOverrideDto> getOverrides(UUID supervisorId, LocalDate from, LocalDate to) {
        List<SupervisorTimetableOverride> overrides = overrideRepository
                .findBySupervisorIdAndOverrideDateBetweenOrderByOverrideDateAscSlotAsc(supervisorId, from, to);
        
        Set<UUID> paathshaalaIds = overrides.stream()
                .map(SupervisorTimetableOverride::getPaathshaalaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
                
        Map<UUID, String> pNames = paathshalaRepository.findAllById(paathshaalaIds).stream()
                .collect(Collectors.toMap(Paathshaala::getId, Paathshaala::getName));
                
        return overrides.stream().map(o -> new TimetableOverrideDto(
                o.getOverrideDate(), o.getSlot(), o.getPaathshaalaId(), pNames.get(o.getPaathshaalaId())
        )).collect(Collectors.toList());
    }
}
