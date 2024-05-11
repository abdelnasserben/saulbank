package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int roleId;

    @ManyToOne
    @JoinColumn(name = "username")
    private User user;

    private String name;

    public Role(User user, String name) {
        this.user = user;
        this.name = name;
    }
}
