package fr.academy.cdp.domain.service;

import fr.academy.cdp.domain.model.DungeonSession;
import fr.academy.cdp.domain.model.PortalSettings;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonSessionManager {
    private static final Map<BlockPos, DungeonSession> sessions = new HashMap<>();

    public static DungeonSession getOrCreateSession(BlockPos pos, PortalSettings settings) {
        return sessions.computeIfAbsent(pos, k -> new DungeonSession(settings,pos));
    }

    public static DungeonSession getSession(BlockPos pos) {
        return sessions.get(pos);
    }

    public static DungeonSession getPlayerSession(UUID playerUuid) {
        return sessions.values().stream()
                .filter(session -> session.getPlayers().contains(playerUuid))
                .findFirst()
                .orElse(null);
    }

    public static void closeSession(BlockPos pos) {
        sessions.remove(pos);
    }

    public static void removeSession(BlockPos pos) {
        sessions.remove(pos);
    }

    private static final Map<UUID, net.minecraft.nbt.NbtList> inventoryStorage = new HashMap<>();

    public static void storeInventory(UUID uuid, net.minecraft.nbt.NbtList list) {
        inventoryStorage.put(uuid, list);
    }

    // Vérifie si un inventaire est stocké sans le supprimer
    public static boolean hasInventory(UUID uuid) {
        return inventoryStorage.containsKey(uuid);
    }

    // Récupère l'inventaire ET le supprime de la mémoire
    public static net.minecraft.nbt.NbtList loadInventory(UUID uuid) {
        return inventoryStorage.remove(uuid);
    }

}