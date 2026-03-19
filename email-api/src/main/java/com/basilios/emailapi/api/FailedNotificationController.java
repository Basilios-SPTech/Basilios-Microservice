package com.basilios.emailapi.api;

import com.basilios.emailapi.domain.model.FailedNotification;
import com.basilios.emailapi.infra.repository.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller para consultar notificações que falharam.
 * Permite monitoramento e reprocessamento manual.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class FailedNotificationController {

    private final FailedNotificationRepository failedNotificationRepository;

    @GetMapping("/failed")
    public ResponseEntity<List<FailedNotification>> getFailedNotifications() {
        List<FailedNotification> failed = failedNotificationRepository.findByResolvedFalseOrderByCreatedAtAsc();
        return ResponseEntity.ok(failed);
    }

}
