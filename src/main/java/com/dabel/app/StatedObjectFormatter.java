package com.dabel.app;

import com.dabel.constant.Status;
import com.dabel.dto.StatedObject;

import java.util.List;

public final class StatedObjectFormatter {
    public static <T extends StatedObject> T format(T t) {
        if(t != null)
            t.setStatus(Status.nameOf(t.getStatus()));
        return t;
    }

    public static <T extends StatedObject> List<T> format(List<T> list) {
        return list.isEmpty() ? list : list.stream()
                .peek(t -> t.setStatus(Status.nameOf(t.getStatus())))
                .toList();
    }
}
