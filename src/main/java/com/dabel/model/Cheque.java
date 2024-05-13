package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cheques")
public class Cheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequeId;

    @ManyToOne
    @JoinColumn(name = "serial")
    private ChequeRequest serial;

    @ManyToOne
    @JoinColumn(name = "trunkId")
    private Trunk trunk;

    private String chequeNumber;

    private String failureReason;

    private String status;
    private String initiatedBy;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @CurrentTimestamp(event = EventType.UPDATE)
    private LocalDateTime updatedAt;
}
