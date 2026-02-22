package fr.academy.cdp.domain.service;

import java.util.HashSet;
import java.util.Set;

public class GridManager {
    private static final int SLOT_DISTANCE = 1000;
    private static final Set<Integer> OCCUPIED_SLOTS = new HashSet<>();

    /**
     * Trouve le premier slot X libre (0, 1000, 2000...)
     */
    public static int findFreeSlot() {
        int slot = 0;
        while (OCCUPIED_SLOTS.contains(slot)) {
            slot += SLOT_DISTANCE;
        }
        OCCUPIED_SLOTS.add(slot);
        return slot;
    }

    public static void releaseSlot(int x) {
        OCCUPIED_SLOTS.remove(x);
    }
}