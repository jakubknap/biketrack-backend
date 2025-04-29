package pl.biketrack.common.entity.auditable;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.biketrack.common.entity.BaseEntity;

import java.time.ZonedDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class DateAuditEntity extends BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private ZonedDateTime lastModifiedDate;
}