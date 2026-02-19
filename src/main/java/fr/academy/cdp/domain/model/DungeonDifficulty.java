package fr.academy.cdp.domain.model;

public enum DungeonDifficulty {
    EASY(-3),     // 🌶️
    NORMAL(-1),   // 🌶️🌶️
    CORSE(0),     // 🌶️🌶️🌶️
    EPICE(2),     // 🌶️🌶️🌶️🌶️
    HARDCORE(5);  // 🌶️🌶️🌶️🌶️🌶️

    private final int offset;
    DungeonDifficulty(int offset) { this.offset = offset; }
    public int getOffset() { return offset; }
}