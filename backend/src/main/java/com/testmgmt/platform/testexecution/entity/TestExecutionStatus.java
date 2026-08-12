package com.testmgmt.platform.testexecution.entity;

public enum TestExecutionStatus {
    NOT_RUN,
    IN_PROGRESS,
    PASSED,
    FAILED,
    BLOCKED,
    SKIPPED,
    NOT_APPLICABLE;

    public boolean isTerminal() {
        return this == PASSED || this == FAILED || this == BLOCKED || this == SKIPPED || this == NOT_APPLICABLE;
    }
}
