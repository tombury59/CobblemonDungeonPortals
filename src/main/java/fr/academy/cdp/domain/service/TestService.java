package fr.academy.cdp.domain.service;

import fr.academy.cdp.CDPMod; // Import indispensable pour trouver PORTAL_BLOCK
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos; // Import indispensable pour BlockPos
import net.minecraft.world.World;

public class TestService {

    public static final RegistryKey<World> DUNGEON_WORLD_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of("cdp", "dungeon_world")
    );

    public void executeTestSpawn(ServerPlayerEntity player) {
        var server = player.getServer();
        if (server == null) return;

        var destinationWorld = server.getWorld(DUNGEON_WORLD_KEY);

        if (destinationWorld != null) {
            // Position sécurisée à la couche 64
            BlockPos portalPos = new BlockPos(0, 64, 0);

            // Pose du bloc (CDPMod est maintenant reconnu grâce à l'import)
            destinationWorld.setBlockState(portalPos, CDPMod.PORTAL_BLOCK.getDefaultState());

            // Téléportation du joueur sur le bloc
            player.teleport(destinationWorld, 0.5, 65.5, 0.5, player.getYaw(), player.getPitch());
        }
    }
}