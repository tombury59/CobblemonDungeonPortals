package fr.academy.cdp.domain.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.domain.model.DungeonSession;
import fr.academy.cdp.infrastructure.service.StructureService;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.UUID;
import java.util.List;

public class TestService {
    public static final RegistryKey<World> DUNGEON_WORLD_KEY = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("cdp", "dungeon_world"));

    /**
     * Lance l'entrée dans le donjon pour un joueur.
     * Gère la Grid et la Structure si c'est le premier joueur.
     */
    public void executeTestSpawn(ServerPlayerEntity player, int levelCap) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerWorld destWorld = server.getWorld(DUNGEON_WORLD_KEY);
        if (destWorld == null) return;

        DungeonSession session = DungeonSessionManager.getPlayerSession(player.getUuid());
        if (session == null) return;

        // --- 1. GESTION DE LA GRID & STRUCTURE ---
        // On initialise le slot seulement si la session n'en a pas encore (premier joueur)
        if (session.getGridX() == -1) {
            int slotX = GridManager.findFreeSlot();
            session.setGridX(slotX);

            // Choix de la structure selon le mode
            String structureName = session.getSettings().mode().equalsIgnoreCase("WAVE")
                    ? "wave_arena"
                    : "clear_tours_1";

            // Placement physique de la structure NBT
            StructureService.placeDungeonStructure(destWorld, new BlockPos(slotX, 64, 0), structureName);
        }

        int currentSlotX = session.getGridX();

        // --- 2. PRÉPARATION DU JOUEUR (Hardcore No-Heal Logic) ---
        saveAndClearInventory(player);

        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : party) {
            pokemon.heal(); // Soin complet à l'entrée

            // Bridage du niveau (Scaling)
            if (pokemon.getLevel() > levelCap) {
                pokemon.getPersistentData().putInt("CDP_OriginalLevel", pokemon.getLevel());
                pokemon.setLevel(levelCap);
            }
        }

        // --- 3. TÉLÉPORTATION ---
        // On TP au centre du slot X (offset 0.5 pour éviter d'être dans un mur)
        player.teleport(destWorld, currentSlotX + 8.5, 65.0, 8.5, player.getYaw(), player.getPitch());
        player.changeGameMode(GameMode.ADVENTURE);

        // Effets de confort pour éviter la mort par faim ou chute au spawn
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 100, 10, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 255, true, false));

        player.sendMessage(Text.literal("§a[CDP] Bienvenue dans le donjon !"), false);
    }

    /**
     * Sortie propre du donjon.
     */
    public void teleportBack(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID uuid = player.getUuid();
        DungeonSession session = DungeonSessionManager.getPlayerSession(uuid);

        if (session != null) {
            session.getPlayers().remove(uuid);

            // Si c'est le dernier joueur, on nettoie la Grid
            if (session.getPlayers().isEmpty()) {
                cleanupDungeon(server, session);
            }
        }

        // Restauration Data
        restoreInventory(player);
        restorePokemonLevels(player);

        // Reset Physique
        player.clearStatusEffects();
        player.changeGameMode(GameMode.SURVIVAL);

        // Retour Overworld
        BlockPos spawn = server.getOverworld().getSpawnPos();
        player.teleport(server.getOverworld(), spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5, 0, 0);

        player.sendMessage(Text.literal("§6[CDP] Session terminée. État restauré."), false);
    }

    /**
     * Nettoyage de la zone dans la dimension Donjon.
     */
    private void cleanupDungeon(MinecraftServer server, DungeonSession session) {
        ServerWorld dungeonWorld = server.getWorld(DUNGEON_WORLD_KEY);
        if (dungeonWorld != null) {
            int x = session.getGridX();
            // On libère le slot dans le manager
            GridManager.releaseSlot(x);

            // Suppression du portail dans l'Overworld
            BlockPos portalPos = session.getPortalPos();
            server.getOverworld().setBlockState(portalPos, net.minecraft.block.Blocks.AIR.getDefaultState());

            DungeonSessionManager.removeSession(portalPos);

            // Note: Une suppression réelle des blocs via itérateur peut être ajoutée ici
            // pour optimiser les performances si nécessaire.
        }
    }

    public void forceCleanExit(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SURVIVAL);
        player.clearStatusEffects();
        restoreInventory(player);
        restorePokemonLevels(player);

        UUID uuid = player.getUuid();
        DungeonSession session = DungeonSessionManager.getPlayerSession(uuid);
        if (session != null) {
            session.getPlayers().remove(uuid);
            if (session.getPlayers().isEmpty() && player.getServer() != null) {
                cleanupDungeon(player.getServer(), session);
            }
        }
    }

    private void saveAndClearInventory(ServerPlayerEntity player) {
        NbtList inventoryNbt = new NbtList();
        player.getInventory().writeNbt(inventoryNbt);
        DungeonSessionManager.storeInventory(player.getUuid(), inventoryNbt);

        player.getInventory().clear();
        player.getInventory().markDirty();
    }

    private void restoreInventory(ServerPlayerEntity player) {
        NbtList savedInv = DungeonSessionManager.loadInventory(player.getUuid());
        if (savedInv != null) {
            player.getInventory().clear();
            player.getInventory().readNbt(savedInv);
            player.getInventory().markDirty();
        }
    }

    private void restorePokemonLevels(ServerPlayerEntity player) {
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (var pokemon : party) {
            if (pokemon.getPersistentData().contains("CDP_OriginalLevel")) {
                int originalLevel = pokemon.getPersistentData().getInt("CDP_OriginalLevel");
                pokemon.setLevel(originalLevel);
                pokemon.getPersistentData().remove("CDP_OriginalLevel");
            }
        }
    }
}