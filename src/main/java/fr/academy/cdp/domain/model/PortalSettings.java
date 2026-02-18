package fr.academy.cdp.domain.model;

import java.util.Random;

public record PortalSettings(
        int levelCap,
        String mode, // "WAVE" ou "CLEAR"
        DungeonDifficulty difficulty,
        String type1,
        String type2
) {
    // Logique de génération aléatoire (Point 7 du sujet)
    public static PortalSettings generateRandom() {
        Random r = new Random();
        int[] caps = {20, 40, 60, 80, 100};
        DungeonDifficulty[] diffs = DungeonDifficulty.values();
        String[] types = {"Fire", "Water", "Grass", "Electric", "Dragon"}; // à compléter

        return new PortalSettings(
                caps[r.nextInt(caps.length)],
                r.nextBoolean() ? "WAVE" : "CLEAR",
                diffs[r.nextInt(diffs.length)],
                types[r.nextInt(types.length)],
                r.nextBoolean() ? types[r.nextInt(types.length)] : null
        );
    }
}