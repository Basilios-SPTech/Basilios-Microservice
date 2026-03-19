package com.basilios.emailapi.infra.repository;

import com.basilios.emailapi.domain.model.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {

    List<FailedNotification> findByResolvedFalseOrderByCreatedAtAsc();

}
