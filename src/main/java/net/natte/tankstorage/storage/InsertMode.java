package net.natte.tankstorage.storage;

public enum InsertMode {
    ALL,
    FILTERED,
    VOID_OVERFLOW;

    public InsertMode next() {
        return switch (this) {
            case ALL -> FILTERED;
            case FILTERED -> VOID_OVERFLOW;
            case VOID_OVERFLOW -> ALL;
        };
    }
}
