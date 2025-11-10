package iwaproject.transaction.enums;

public enum TransitionState {
    EXCHANGING,
    REQUESTED,
    REQUEST_ACCEPTED,
    PREPAYED,
    CLIENT_CONFIRMED,
    PROVIDER_CONFIRMED,
    DOUBLE_CONFIRMED,
    FINISHED_AND_PAYED,
    CANCELED;
}
