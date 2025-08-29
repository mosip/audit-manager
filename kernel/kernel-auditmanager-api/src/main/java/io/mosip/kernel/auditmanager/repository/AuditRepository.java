/**
 * 
 */
package io.mosip.kernel.auditmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.kernel.auditmanager.entity.Audit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface with data access and data modification functions on
 * {@link Audit}
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface AuditRepository extends JpaRepository<Audit, String> {

    /**
     * Batch update audits with the provided entities
     *
     * @param audits List of Audit entities to update
     * @return Number of records updated
     */
    @Transactional
    @Modifying
    @Query("UPDATE Audit a SET a.eventId = :#{#audit.eventId}, a.eventName = :#{#audit.eventName}, "
            + "a.eventType = :#{#audit.eventType}, a.actionTimeStamp = :#{#audit.actionTimeStamp}, "
            + "a.hostName = :#{#audit.hostName}, a.hostIp = :#{#audit.hostIp}, "
            + "a.applicationId = :#{#audit.applicationId}, a.applicationName = :#{#audit.applicationName}, "
            + "a.sessionUserId = :#{#audit.sessionUserId}, a.sessionUserName = :#{#audit.sessionUserName}, "
            + "a.id = :#{#audit.id}, a.idType = :#{#audit.idType}, a.createdBy = :#{#audit.createdBy}, "
            + "a.createdAt = :#{#audit.createdAt}, a.moduleName = :#{#audit.moduleName}, "
            + "a.moduleId = :#{#audit.moduleId}, a.description = :#{#audit.description} "
            + "WHERE a.id = :#{#audit.id}")
    int batchUpdate(@Param("audit") List<Audit> audits);

    /**
     * Delete audits older than the specified cutoff time
     *
     * @param cutoffTime The cutoff time for deleting old audits
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Audit a WHERE a.createdAt < :cutoffTime")
    int deleteAuditsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
