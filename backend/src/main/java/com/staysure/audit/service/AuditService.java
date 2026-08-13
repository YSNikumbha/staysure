package com.staysure.audit.service;

import com.staysure.audit.entity.AuditLog;
import com.staysure.audit.repository.AuditLogRepository;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User actor, String action, String module, String entityType, Object entityId,
                    String description, String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setModule(module);
        log.setEntityType(entityType);
        log.setEntityId(entityId == null ? null : String.valueOf(entityId));
        log.setDescription(description);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}
