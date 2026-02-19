package fr.academy.cdp.domain.model;

import java.util.Random;

public record PortalSettings(
        int levelCap,
        String mode,
        DungeonDifficulty difficulty,
        String type1,
        String type2
) {
    /**
     * Calcule le niveau d'un ennemi selon la formule du cahier des charges.
     * @param waveOffset L'éventuel malus de niveau selon la vague (0 pour exploration)
     * @return Le niveau calculé pour le Pokémon ennemi
     */
    public int calculateEnemyLevel(int waveOffset) {
        // Formule : (Cap - OffsetVague) + OffsetPiment
        int level = (this.levelCap - waveOffset) + this.difficulty.getOffset();

        // Sécurité : Un Pokémon ne peut pas être niveau < 1 ou > 100
        return Math.max(1, Math.min(100, level));
    }

    /**
     * Retourne le nombre de piments pour l'UI (de 1 à 5)
     */
    public int getPimentScore() {
        return this.difficulty.ordinal() + 1;
    }

    public static PortalSettings generateRandom() {
        Random r = new Random();
        int[] caps = {20, 40, 60, 80, 100};
        DungeonDifficulty[] diffs = DungeonDifficulty.values();
        String[] types = {"Fire", "Water", "Grass", "Electric", "Dragon", "Ice", "Ghost", "Dark", "Steel", "Fairy"};

        String t1 = types[r.nextInt(types.length)];
        // On évite d'avoir deux fois le même type
        String t2 = r.nextBoolean() ? types[r.nextInt(types.length)] : "";
        if (t1.equals(t2)) t2 = "";

        return new PortalSettings(
                caps[r.nextInt(caps.length)],
                r.nextBoolean() ? "WAVE" : "CLEAR",
                diffs[r.nextInt(diffs.length)],
                t1,
                t2
        );
    }
}