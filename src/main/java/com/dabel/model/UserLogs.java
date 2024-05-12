package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_logs")
public class UserLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long logId;

    private String username;

    private String method;

    private String status;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;
}
