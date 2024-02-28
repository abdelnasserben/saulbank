package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trunks")
public class Trunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trunkId;
    @ManyToOne
    private Account account;
    @ManyToOne
    private Customer customer;
    private String membership;
}
