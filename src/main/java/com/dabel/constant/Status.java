package com.dabel.constant;

public enum Status {
    PENDING(0),
    ACTIVE(1),
    FAILED(2),
    APPROVED(3),
    REJECTED(4),
    DEACTIVATED(5);

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
