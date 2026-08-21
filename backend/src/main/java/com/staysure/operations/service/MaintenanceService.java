package com.staysure.operations.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.operations.dto.MaintenanceTaskRequest;
import com.staysure.operations.dto.MaintenanceTaskResponse;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.MaintenanceTask;
import com.staysure.operations.enums.MaintenanceStatus;
import com.staysure.operations.enums.OperationalPriority;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.ComplaintRepository;
import com.staysure.operations.repository.MaintenanceTaskRepository;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceTaskRepository taskRepository;
    private final ComplaintRepository complaintRepository;
    private final OperationAccessService accessService;
    private final AuditService auditService;
    private final OperationMapper mapper;

    public MaintenanceService(MaintenanceTaskRepository taskRepository,
                              ComplaintRepository complaintRepository,
                              OperationAccessService accessService,
                              AuditService auditService,
                              OperationMapper mapper) {
        this.taskRepository = taskRepository;
        this.complaintRepository = complaintRepository;
        this.accessService = accessService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> list(Long ownerUserId) {
        return taskRepository.findAllByOwner(accessService.owner(ownerUserId)).stream().map(mapper::toMaintenance).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceTaskResponse get(Long ownerUserId, Long taskId) {
        return mapper.toMaintenance(ownerTask(ownerUserId, taskId));
    }

    @Transactional
    public MaintenanceTaskResponse create(Long ownerUserId, MaintenanceTaskRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        OwnerProfile owner = accessService.owner(ownerUserId);
        PgProperty property = accessService.ownedProperty(request.propertyId(), owner);
        Complaint complaint = null;
        if (request.complaintId() != null) {
            complaint = complaintRepository.findByIdAndOwner(request.complaintId(), owner)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Complaint access denied", "COMPLAINT_ACCESS_DENIED"));
            if (!complaint.getProperty().getId().equals(property.getId())) {
                throw new BusinessRuleException("Maintenance task property must match complaint property", "INVALID_MAINTENANCE_TASK");
            }
        }
        MaintenanceTask task = new MaintenanceTask();
        task.setTaskNumber(nextTaskNumber());
        task.setProperty(property);
        task.setComplaint(complaint);
        task.setRoom(complaint == null ? null : complaint.getRoom());
        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        task.setPriority(request.priority() == null ? OperationalPriority.MEDIUM : request.priority());
        task.setAssignedToText(blankToNull(request.assignedToText()));
        task.setScheduledDate(request.scheduledDate());
        task.setRemarks(blankToNull(request.remarks()));
        task.setStatus(request.scheduledDate() == null ? MaintenanceStatus.PENDING : MaintenanceStatus.SCHEDULED);
        MaintenanceTask saved = taskRepository.save(task);
        auditService.log(actor, "MAINTENANCE_TASK_CREATED", "OPERATIONS", "MaintenanceTask", saved.getId(),
                "Maintenance task created", null, saved.getTaskNumber(), ipAddress);
        return mapper.toMaintenance(saved);
    }

    @Transactional
    public MaintenanceTaskResponse update(Long ownerUserId, Long taskId, MaintenanceTaskRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        MaintenanceTask task = ownerTask(ownerUserId, taskId);
        if (task.getStatus() == MaintenanceStatus.COMPLETED || task.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new BusinessRuleException("Finalized maintenance tasks cannot be edited", "INVALID_MAINTENANCE_STATUS");
        }
        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        task.setPriority(request.priority() == null ? OperationalPriority.MEDIUM : request.priority());
        task.setAssignedToText(blankToNull(request.assignedToText()));
        task.setScheduledDate(request.scheduledDate());
        task.setRemarks(blankToNull(request.remarks()));
        if (task.getStatus() == MaintenanceStatus.PENDING && task.getScheduledDate() != null) {
            task.setStatus(MaintenanceStatus.SCHEDULED);
        }
        MaintenanceTask saved = taskRepository.save(task);
        auditService.log(actor, "MAINTENANCE_TASK_UPDATED", "OPERATIONS", "MaintenanceTask", saved.getId(),
                "Maintenance task updated", null, saved.getStatus().name(), ipAddress);
        return mapper.toMaintenance(saved);
    }

    @Transactional
    public MaintenanceTaskResponse start(Long ownerUserId, Long taskId, OperationActionRequest request, String ipAddress) {
        MaintenanceTask task = ownerTask(ownerUserId, taskId);
        if (task.getStatus() != MaintenanceStatus.PENDING && task.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new BusinessRuleException("Maintenance task cannot be started", "INVALID_MAINTENANCE_STATUS");
        }
        return transition(ownerUserId, task, MaintenanceStatus.IN_PROGRESS, "MAINTENANCE_TASK_STARTED", request, ipAddress);
    }

    @Transactional
    public MaintenanceTaskResponse complete(Long ownerUserId, Long taskId, OperationActionRequest request, String ipAddress) {
        MaintenanceTask task = ownerTask(ownerUserId, taskId);
        if (task.getStatus() != MaintenanceStatus.IN_PROGRESS && task.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new BusinessRuleException("Maintenance task cannot be completed", "INVALID_MAINTENANCE_STATUS");
        }
        task.setCompletedAt(LocalDateTime.now());
        return transition(ownerUserId, task, MaintenanceStatus.COMPLETED, "MAINTENANCE_TASK_COMPLETED", request, ipAddress);
    }

    @Transactional
    public MaintenanceTaskResponse cancel(Long ownerUserId, Long taskId, OperationActionRequest request, String ipAddress) {
        MaintenanceTask task = ownerTask(ownerUserId, taskId);
        if (task.getStatus() == MaintenanceStatus.COMPLETED || task.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new BusinessRuleException("Maintenance task cannot be cancelled", "INVALID_MAINTENANCE_STATUS");
        }
        return transition(ownerUserId, task, MaintenanceStatus.CANCELLED, "MAINTENANCE_TASK_CANCELLED", request, ipAddress);
    }

    private MaintenanceTaskResponse transition(Long ownerUserId, MaintenanceTask task, MaintenanceStatus next,
                                               String action, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        MaintenanceStatus previous = task.getStatus();
        task.setStatus(next);
        if (request != null && request.remarks() != null && !request.remarks().isBlank()) {
            task.setRemarks(request.remarks().trim());
        }
        MaintenanceTask saved = taskRepository.save(task);
        auditService.log(actor, action, "OPERATIONS", "MaintenanceTask", saved.getId(),
                "Maintenance task status updated", previous.name(), next.name(), ipAddress);
        return mapper.toMaintenance(saved);
    }

    private MaintenanceTask ownerTask(Long ownerUserId, Long taskId) {
        return taskRepository.findByIdAndOwner(taskId, accessService.owner(ownerUserId))
                .orElseThrow(() -> {
                    if (taskId != null && taskRepository.existsById(taskId)) {
                        return new ApiException(HttpStatus.FORBIDDEN, "Maintenance task access denied", "MAINTENANCE_TASK_ACCESS_DENIED");
                    }
                    return new ApiException(HttpStatus.NOT_FOUND, "Maintenance task not found", "MAINTENANCE_TASK_NOT_FOUND");
                });
    }

    private String nextTaskNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "MNT-" + year + "-";
        long sequence = taskRepository.countByTaskNumberStartingWith(prefix) + 1;
        String number;
        do {
            number = prefix + String.format("%06d", sequence++);
        } while (taskRepository.existsByTaskNumber(number));
        return number;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
