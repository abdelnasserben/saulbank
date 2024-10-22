package com.dabel.constant;

public enum Status {
    PENDING(0),
    ACTIVE(1),
    INACTIVE(2),
    FAILED(3),
    APPROVED(4),
    REJECTED(5),
    COMPLETED(6),
    USED(7),
    CLOSED(8),
    SUCCESS(9);

    private final int code;
    Status(int code) {
        this.code = code;
    }

    public String code() {
        return String.valueOf(this.code);
    }

    public static String nameOf(String code) {
        int resolveCode = Integer.parseInt(code);
        for(Status status: values()) {
            if(status.code == resolveCode)
                return status.name();
        }
        return PENDING.name();
    }

    public static String codeOf(String name) {
        for(Status status: values()) {
            if(status.name().equals(name))
                return status.code();
        }
        return PENDING.code();
    }
}
