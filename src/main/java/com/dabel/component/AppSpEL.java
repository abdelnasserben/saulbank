package com.dabel.component;

import com.dabel.constant.Status;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppSpEL {

    public String statusColor(String status) {

        if(status.equals(Status.PENDING.name()))
            return "warning";

        return List.of(Status.ACTIVE.name(), Status.APPROVED.name()).contains(status) ? "success" : "danger";
    }

    public String accountStatusColorByHisBalance(double balance) {
        return balance < 0 ? "danger" : (balance > 0 ? "success" : "warning");
    }
}
