package fr.academy.cdp.domain.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.domain.model.DungeonSession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import java.util.List;

public class TestService {
    public static final RegistryKey<World> DUNGEON_WORLD_KEY = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("cdp", "dungeon_world"));

    // Appelé quand le chef lance l'instance
    public void executeTestSpawn(ServerPlayerEntity player, int levelCap) {
        var server = player.getServer();
        if (server == null) return;

        var destWorld = server.getWorld(DUNGEON_WORLD_KEY);
        if (destWorld != null) {
            var party = Cobblemon.INSTANCE.getStorage().getParty(player);

            for (Pokemon pokemon : party) {
                // --- AJOUT : SOIN AVANT L'ENTRÉE ---
                pokemon.heal();



                // Bridage des niveaux
                if (pokemon.getLevel() > levelCap) {
                    pokemon.getPersistentData().putInt("CDP_OriginalLevel", pokemon.getLevel());
                    pokemon.setLevel(levelCap);
                }
            }

            // Message de confirmation
            player.sendMessage(Text.literal("§a[CDP] Vos Pokémon ont été soignés et préparés !"), false);

            // Téléportation (Position arbitraire pour le test)
            player.teleport(destWorld, 0.5, 65.5, 0.5, player.getYaw(), player.getPitch());
        }
    }

    public void teleportBack(ServerPlayerEntity player) {
        var server = player.getServer();
        if (server == null) return;

        var session = DungeonSessionManager.getPlayerSession(player.getUuid());
        if (session != null) {
            session.getPlayers().remove(player.getUuid());

            // DESTRUCTION SI VIDE
            if (session.getPlayers().isEmpty()) {
                BlockPos pos = session.getPortalPos();
                server.getOverworld().setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                DungeonSessionManager.removeSession(pos);
            }
        }

        // Restauration Pokémon
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : party) {
            if (pokemon.getPersistentData().contains("CDP_OriginalLevel")) {
                pokemon.setLevel(pokemon.getPersistentData().getInt("CDP_OriginalLevel"));
                pokemon.getPersistentData().remove("CDP_OriginalLevel");
            }
        }

        player.teleport(server.getOverworld(), server.getOverworld().getSpawnPos().getX(), server.getOverworld().getSpawnPos().getY() + 1, server.getOverworld().getSpawnPos().getZ(), 0, 0);
        player.sendMessage(Text.literal("§6[CDP] Fin d'instance : Niveaux restaurés."), false);
    }

    public List<String> getSessionPlayerNames(MinecraftServer server, DungeonSession session) {
        return session.getPlayers().stream()
                .map(uuid -> {
                    var p = server.getPlayerManager().getPlayer(uuid);
                    return p != null ? p.getNameForScoreboard() : "Unknown";
                }).toList();
    }
}