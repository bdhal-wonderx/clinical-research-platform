package com.wonderx.rwe.service;

import com.wonderx.rwe.entity.AuditLog;
import com.wonderx.rwe.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityType, UUID entityId, String action, String performedBy,
                    String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(auditLog);
    }
}
