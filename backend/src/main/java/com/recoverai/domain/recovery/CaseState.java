package com.recoverai.domain.recovery;

public enum CaseState {
    RECEIVED,
    ELIGIBLE,
    DIAGNOSING,
    ACTION_PENDING,
    ACTION_EXECUTING,
    WAITING,
    RECOVERED,
    RETRY,
    ESCALATED,
    STOPPED
}
