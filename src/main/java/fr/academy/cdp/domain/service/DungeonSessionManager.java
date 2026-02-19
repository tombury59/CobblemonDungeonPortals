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
        return sessions.computeIfAbsent(pos, k -> new DungeonSession(settings));
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
}