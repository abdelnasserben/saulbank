package com.dabel.repository;

import com.dabel.model.Role;
import com.dabel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByUser(User user);
}
