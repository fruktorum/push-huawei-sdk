package com.devinotele.huaweidevinosdk.sdk;

class DevinoErrorMessage {
    private final String event;
    private final String code;
    private final String message;

    DevinoErrorMessage(String event, String code, String message) {
        this.event = event;
        this.code = code;
        this.message = message;
    }

    String getMessage() {
        return event + ": ERROR\n" + "code: " + code + ",  message: " + message;
    }
}