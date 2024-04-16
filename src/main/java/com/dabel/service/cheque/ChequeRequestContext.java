package com.dabel.service.cheque;

import com.dabel.exception.IllegalOperationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ChequeRequestContext {

    private final Map<String, ChequeRequest> applicationMap = new HashMap<>();

    public ChequeRequestContext(Set<ChequeRequest> chequeRequests) {
        chequeRequests.forEach(type -> applicationMap.put(type.getType().name(), type));
    }

    public ChequeRequest setContext(String applicationType) {
        return Optional.ofNullable(applicationMap.get(applicationType)).orElseThrow(() -> new IllegalOperationException("Unknown cheque application type"));
    }
}
