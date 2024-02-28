package com.dabel.service.fee;

import com.dabel.exception.IllegalOperationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class FeeContext {

    private final Map<String, Tax> chargeMap = new HashMap<>();

    public FeeContext(Set<Tax> taxes) {
        taxes.forEach(type -> chargeMap.put(type.getLedgerType().name(), type));
    }

    public Tax setContext(String ledgerType) {
        return Optional.ofNullable(chargeMap.get(ledgerType)).orElseThrow(() -> new IllegalOperationException("Unknown fee type"));
    }
}
