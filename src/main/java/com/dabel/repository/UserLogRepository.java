package com.dabel.repository;

import com.dabel.model.User;
import com.dabel.model.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    List<UserLog> findAllByUser(User user);
}
