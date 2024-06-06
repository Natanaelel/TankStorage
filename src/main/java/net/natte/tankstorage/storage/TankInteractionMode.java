package net.natte.tankstorage.storage;

public enum TankInteractionMode {
    OPEN_SCREEN,
    BUCKET;

    public TankInteractionMode next() {
        return switch (this) {
            case OPEN_SCREEN -> BUCKET;
            case BUCKET -> OPEN_SCREEN;
        };
    }
}
