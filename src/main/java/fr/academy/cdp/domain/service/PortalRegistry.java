package fr.academy.cdp.domain.service;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalRegistry {
    // Stocke l'UUID du joueur -> La position de son portail
    private static final Map<UUID, BlockPos> playerPortals = new HashMap<>();

    public static void registerPortal(UUID playerUuid, ServerWorld world, BlockPos newPos) {
        // 1. Si le joueur a déjà un portail, on le retire du monde
        if (playerPortals.containsKey(playerUuid)) {
            BlockPos oldPos = playerPortals.get(playerUuid);
            // On vérifie que c'est bien de l'air ou notre bloc pour éviter de casser n'importe quoi
            world.setBlockState(oldPos, net.minecraft.block.Blocks.AIR.getDefaultState());
        }

        // 2. On enregistre la nouvelle position
        playerPortals.put(playerUuid, newPos);
    }
}