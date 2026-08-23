package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.PaathshalaRepository;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository       userRepository;
    private final PaathshalaRepository paathshalaRepository;
    private final com.pathshala.stub.repository.LocationPointRepository locationPointRepository;

    public UserService(UserRepository userRepository,
                       PaathshalaRepository paathshalaRepository,
                       com.pathshala.stub.repository.LocationPointRepository locationPointRepository) {
        this.userRepository       = userRepository;
        this.paathshalaRepository = paathshalaRepository;
        this.locationPointRepository = locationPointRepository;
    }

    // ── Supervisors ───────────────────────────────────────────────────

    @Transactional
    public SupervisorResponse createSupervisor(CreateSupervisorRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole("supervisor");
        user.setActive(true);
        // assignedPaathshalaId stays null for supervisors
        return toSupervisorResponse(userRepository.save(user), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupervisorResponse> findAllSupervisors(Pageable pageable) {
        Page<User> page = userRepository.findByRoleOrderByCreatedAtDesc("supervisor", pageable);
        
        List<UUID> userIds = page.getContent().stream().map(User::getId).collect(Collectors.toList());
        Map<UUID, com.pathshala.stub.entity.LocationPoint> latestLocations = userIds.isEmpty() ? Map.of() :
            locationPointRepository.findLatestPingsForUsers(userIds).stream()
                .collect(Collectors.toMap(com.pathshala.stub.entity.LocationPoint::getUserId, Function.identity()));

        Page<SupervisorResponse> responsePage = page.map(u -> 
            toSupervisorResponse(u, latestLocations.get(u.getId())));
            
        return PagedResponse.of(responsePage);
    }

    @Transactional
    public SupervisorResponse updateSupervisor(UUID id, UpdateSupervisorRequest request) {
        User user = findUserByIdAndRole(id, "supervisor");

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }
        return toSupervisorResponse(userRepository.save(user), null);
    }

    @Transactional(readOnly = true)
    public SupervisorResponse findSupervisorById(UUID id) {
        User user = findUserByIdAndRole(id, "supervisor");
        return toSupervisorResponse(user, null);
    }

    @Transactional
    public void deactivateSupervisor(UUID id) {
        User user = findUserByIdAndRole(id, "supervisor");
        user.setActive(false);
        userRepository.save(user);
    }

    // ── Teachers ─────────────────────────────────────────────────────

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        // Validate the paathshaala exists before creating the teacher
        Paathshaala paathshaala = paathshalaRepository
                .findById(request.paathshalaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Paathshaala not found: " + request.paathshalaId()
                                + " — please provide a valid paathshaala_id"));

        User user = new User();
        user.setName(request.name());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole("teacher");
        user.setAssignedPaathshalaId(paathshaala.getId());
        user.setActive(true);

        return toTeacherResponse(userRepository.save(user), paathshaala.getName(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TeacherResponse> findAllTeachers(Pageable pageable) {
        Page<User> teacherPage = userRepository
                .findByRoleOrderByCreatedAtDesc("teacher", pageable);

        // Batch-fetch all referenced paathshaalas to avoid N+1
        Set<UUID> paathshalaIds = teacherPage.getContent().stream()
                .map(User::getAssignedPaathshalaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Paathshaala> paathshalaMap = paathshalaRepository
                .findAllById(paathshalaIds)
                .stream()
                .collect(Collectors.toMap(Paathshaala::getId, Function.identity()));
                
        List<UUID> userIds = teacherPage.getContent().stream().map(User::getId).collect(Collectors.toList());
        Map<UUID, com.pathshala.stub.entity.LocationPoint> latestLocations = userIds.isEmpty() ? Map.of() :
            locationPointRepository.findLatestPingsForUsers(userIds).stream()
                .collect(Collectors.toMap(com.pathshala.stub.entity.LocationPoint::getUserId, Function.identity()));

        Page<TeacherResponse> responsePage = teacherPage.map(teacher -> {
            Paathshaala p = teacher.getAssignedPaathshalaId() != null
                    ? paathshalaMap.get(teacher.getAssignedPaathshalaId())
                    : null;
            return toTeacherResponse(teacher, p != null ? p.getName() : null, latestLocations.get(teacher.getId()));
        });

        return PagedResponse.of(responsePage);
    }

    @Transactional
    public TeacherResponse updateTeacher(UUID id, UpdateTeacherRequest request) {
        User user = findUserByIdAndRole(id, "teacher");

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        String paathshalaName = null;
        if (request.paathshalaId() != null) {
            Paathshaala paathshaala = paathshalaRepository
                    .findById(request.paathshalaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Paathshaala not found: " + request.paathshalaId()));
            user.setAssignedPaathshalaId(paathshaala.getId());
            paathshalaName = paathshaala.getName();
        } else {
            // paathshaala_id not in request — keep existing assignment
            if (user.getAssignedPaathshalaId() != null) {
                paathshalaName = paathshalaRepository
                        .findById(user.getAssignedPaathshalaId())
                        .map(Paathshaala::getName)
                        .orElse(null);
            }
        }

        return toTeacherResponse(userRepository.save(user), paathshalaName, null);
    }

    @Transactional
    public void deactivateTeacher(UUID id) {
        User user = findUserByIdAndRole(id, "teacher");
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TeacherResponse findTeacherById(UUID id) {
        User user = findUserByIdAndRole(id, "teacher");
        String paathshalaName = null;
        if (user.getAssignedPaathshalaId() != null) {
            paathshalaName = paathshalaRepository
                    .findById(user.getAssignedPaathshalaId())
                    .map(Paathshaala::getName)
                    .orElse(null);
        }
        return toTeacherResponse(user, paathshalaName, null);
    }

    // ── Private helpers ───────────────────────────────────────────────

    private User findUserByIdAndRole(UUID id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        role.substring(0, 1).toUpperCase() + role.substring(1) + " not found: " + id));
        if (!role.equals(user.getRole())) {
            throw new NoSuchElementException(
                    role.substring(0, 1).toUpperCase() + role.substring(1) + " not found: " + id);
        }
        return user;
    }

    private SupervisorResponse toSupervisorResponse(User user, com.pathshala.stub.entity.LocationPoint location) {
        return new SupervisorResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.isActive(),
                user.getCreatedAt(),
                location != null ? location.getLat() : null,
                location != null ? location.getLng() : null,
                location != null ? location.getCapturedAt() : null
        );
    }

    private TeacherResponse toTeacherResponse(User user, String paathshalaName, com.pathshala.stub.entity.LocationPoint location) {
        return new TeacherResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.isActive(),
                user.getAssignedPaathshalaId(),
                paathshalaName,
                user.getCreatedAt(),
                location != null ? location.getLat() : null,
                location != null ? location.getLng() : null,
                location != null ? location.getCapturedAt() : null
        );
    }
}
