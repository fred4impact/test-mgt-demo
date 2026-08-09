package com.testmgmt.platform.common.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldErrorDetail> errors) {

    public record FieldErrorDetail(String field, String message) {}
}
