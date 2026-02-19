package fr.academy.cdp.domain.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.CDPMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class TestService {
    public static final RegistryKey<World> DUNGEON_WORLD_KEY = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("cdp", "dungeon_world"));

    public void executeTestSpawn(ServerPlayerEntity player, int levelCap) {
        var server = player.getServer();
        if (server == null) return;

        var destWorld = server.getWorld(DUNGEON_WORLD_KEY);
        if (destWorld != null) {
            var party = Cobblemon.INSTANCE.getStorage().getParty(player);
            for (Pokemon pokemon : party) {
                if (pokemon.getLevel() > levelCap) {
                    // CORRECTION : Utilisation de getPersistentData()
                    // Si cela ne compile toujours pas, utilise : pokemon.getCustomData()
                    pokemon.getPersistentData().putInt("CDP_OriginalLevel", pokemon.getLevel());

                    pokemon.setLevel(levelCap);
                }
            }

            BlockPos pos = new BlockPos(0, 64, 0);
            destWorld.setBlockState(pos, CDPMod.PORTAL_BLOCK.getDefaultState());
            player.teleport(destWorld, 0.5, 65.5, 0.5, player.getYaw(), player.getPitch());
        }
    }

    public void teleportBack(ServerPlayerEntity player) {
        var server = player.getServer();
        if (server == null) return;

        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : party) {
            // CORRECTION : Utilisation de getPersistentData()
            if (pokemon.getPersistentData().contains("CDP_OriginalLevel")) {
                int originalLevel = pokemon.getPersistentData().getInt("CDP_OriginalLevel");

                pokemon.setLevel(originalLevel);
                pokemon.getPersistentData().remove("CDP_OriginalLevel");
            }
        }

        var overworld = server.getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, player.getYaw(), player.getPitch());

        player.sendMessage(Text.literal("§6[CDP] Niveaux restaurés !"), false);
    }
}