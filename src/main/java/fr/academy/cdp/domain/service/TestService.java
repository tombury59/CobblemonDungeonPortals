package fr.academy.cdp.domain.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.CDPMod;
import fr.academy.cdp.domain.model.DungeonSession;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.GameMode;
import java.util.UUID;

import java.util.List;

public class TestService {
    public static final RegistryKey<World> DUNGEON_WORLD_KEY = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("cdp", "dungeon_world"));

    // Identifiant NBT pour la sauvegarde
    private static final String INVENTORY_NBT_KEY = "CDP_StoredInventory";

    /**
     * Appelé quand le chef lance l'instance ou qu'un joueur rejoint un donjon actif.
     */
    public void executeTestSpawn(ServerPlayerEntity player, int levelCap) {
        var server = player.getServer();
        if (server == null) return;

        var destWorld = server.getWorld(DUNGEON_WORLD_KEY);
        if (destWorld != null) {

            // 1. SAUVEGARDE ET VIDE L'INVENTAIRE
            saveAndClearInventory(player);

            // 2. SOIN ET BRIDAGE DES POKÉMON
            var party = Cobblemon.INSTANCE.getStorage().getParty(player);
            for (Pokemon pokemon : party) {
                // Soin complet (PV/PP/Status)
                pokemon.heal();

                // Bridage du niveau si supérieur au cap
                if (pokemon.getLevel() > levelCap) {
                    pokemon.getPersistentData().putInt("CDP_OriginalLevel", pokemon.getLevel());
                    pokemon.setLevel(levelCap);
                }
            }

            // 3. TÉLÉPORTATION
            player.sendMessage(Text.literal("§a[CDP] Pokémon soignés. Inventaire mis en sécurité !"), false);
            // On vise le centre de la structure de test (0, 65, 0)
            player.teleport(destWorld, 0.5, 65.5, 0.5, player.getYaw(), player.getPitch());
            player.changeGameMode(net.minecraft.world.GameMode.ADVENTURE);

            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SATURATION, 999999, 10, true, false));

            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.REGENERATION, 999999, 10, true, false));
        }
    }

    /**
     * Appelé lors de la sortie du donjon (mort, victoire ou abandon).
     */
    public void teleportBack(ServerPlayerEntity player) {
        var server = player.getServer();
        if (server == null) return;

        // 1. GESTION DE LA SESSION
        var session = DungeonSessionManager.getPlayerSession(player.getUuid());
        if (session != null) {
            session.getPlayers().remove(player.getUuid());

            // Si le dernier joueur part, on détruit le portail
            if (session.getPlayers().isEmpty()) {
                BlockPos pos = session.getPortalPos();
                server.getOverworld().setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                DungeonSessionManager.removeSession(pos);
            }
        }

        // 2. RESTAURATION DE L'INVENTAIRE
        restoreInventory(player);

        // 3. RESTAURATION DES NIVEAUX POKÉMON
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : party) {
            if (pokemon.getPersistentData().contains("CDP_OriginalLevel")) {
                pokemon.setLevel(pokemon.getPersistentData().getInt("CDP_OriginalLevel"));
                pokemon.getPersistentData().remove("CDP_OriginalLevel");
            }
        }

        // 4. RETOUR AU SPAWN DU MONDE PRINCIPAL
        BlockPos spawn = server.getOverworld().getSpawnPos();
        player.teleport(server.getOverworld(), spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5, 0, 0);
        player.sendMessage(Text.literal("§6[CDP] Donjon terminé : Inventaire et niveaux restaurés."), false);
        // Dans TestService.java, méthode teleportBack
        player.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
    }

    /**
     * Utilitaires NBT pour l'inventaire
     */
    private void saveAndClearInventory(ServerPlayerEntity player) {
        // 1. Création de la liste NBT (objets moddés inclus)
        NbtList inventoryNbt = new NbtList();
        player.getInventory().writeNbt(inventoryNbt);

        // 2. Sauvegarde dans le Manager (survit tant que le serveur est allumé)
        DungeonSessionManager.storeInventory(player.getUuid(), inventoryNbt);

        // 3. Vidage complet
        player.getInventory().clear();
        player.getInventory().markDirty();
    }

    private void restoreInventory(ServerPlayerEntity player) {
        // Récupération et suppression immédiate du stockage pour éviter les doublons
        NbtList savedInv = DungeonSessionManager.loadInventory(player.getUuid());

        if (savedInv != null) {
            player.getInventory().clear();
            player.getInventory().readNbt(savedInv);
            player.getInventory().markDirty();
        }
    }

    /**
     * Récupère les noms des joueurs pour l'UI
     */
    public List<String> getSessionPlayerNames(MinecraftServer server, DungeonSession session) {
        return session.getPlayers().stream()
                .map(uuid -> {
                    var p = server.getPlayerManager().getPlayer(uuid);
                    return p != null ? p.getNameForScoreboard() : "Unknown";
                }).toList();
    }

    /**
     * Nettoyage forcé si le joueur quitte la dimension par un autre moyen
     */
    public void forceCleanExit(ServerPlayerEntity player) {
        // A. RESET PHYSIQUE
        player.changeGameMode(GameMode.SURVIVAL);

        player.clearStatusEffects();
        player.getHungerManager().setFoodLevel(20);
        player.setFireTicks(0);

        player.stopRiding();           // Sortir d'une monture Pokémon
        player.getAbilities().flying = false; // Stopper le vol si bug
        player.getAbilities().invulnerable = false;
        player.sendAbilitiesUpdate();

        // B. RESET DES EFFETS
        player.clearStatusEffects();   // Enlever buffs/debuffs du donjon
        player.setFireTicks(0);        // Éteindre le feu
        player.setHealth(player.getMaxHealth()); // Soin final pour la sortie

        // C. RESTAURATION DATA
        restoreInventory(player);
        restorePokemonLevels(player);

        // D. NETTOYAGE SESSION
        UUID uuid = player.getUuid();
        var session = DungeonSessionManager.getPlayerSession(uuid);
        if (session != null) {
            session.getPlayers().remove(uuid);
            if (session.getPlayers().isEmpty()) {
                // Nettoyage du bloc portail et de la session
                var server = player.getServer();
                if (server != null) {
                    server.getOverworld().setBlockState(session.getPortalPos(), net.minecraft.block.Blocks.AIR.getDefaultState());
                    DungeonSessionManager.removeSession(session.getPortalPos());
                }
            }
        }
    }

    private void restorePokemonLevels(ServerPlayerEntity player) {
        var party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player);
        for (var pokemon : party) {
            if (pokemon.getPersistentData().contains("CDP_OriginalLevel")) {
                int originalLevel = pokemon.getPersistentData().getInt("CDP_OriginalLevel");
                pokemon.setLevel(originalLevel);
                pokemon.getPersistentData().remove("CDP_OriginalLevel");
            }
        }
    }
}