package com.dabel.repository;

import com.dabel.model.LoginLog;
import com.dabel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    List<LoginLog> findAllByUser(User user);
}
