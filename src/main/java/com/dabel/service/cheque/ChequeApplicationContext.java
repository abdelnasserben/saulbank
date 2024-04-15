package com.dabel.service.cheque;

import com.dabel.exception.IllegalOperationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ChequeApplicationContext {

    private final Map<String, ChequeApplication> applicationMap = new HashMap<>();

    public ChequeApplicationContext(Set<ChequeApplication> chequeApplications) {
        chequeApplications.forEach(type -> applicationMap.put(type.getType().name(), type));
    }

    public ChequeApplication setContext(String applicationType) {
        return Optional.ofNullable(applicationMap.get(applicationType)).orElseThrow(() -> new IllegalOperationException("Unknown cheque application type"));
    }
}
