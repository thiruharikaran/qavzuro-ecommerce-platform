package com.qavzuro.service;

import com.qavzuro.domain.AuditLog;
import com.qavzuro.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String actorUserId, String actorEmail, String action, String resourceType,
                        String resourceId, Map<String, Object> metadata) {
        String ip = currentIp();
        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .actorEmail(actorEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .metadata(metadata == null ? Map.of() : metadata)
                .ipAddress(ip)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> list(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
