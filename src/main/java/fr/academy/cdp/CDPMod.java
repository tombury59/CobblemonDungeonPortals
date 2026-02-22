package fr.academy.cdp;

import fr.academy.cdp.domain.model.DungeonSession;
import fr.academy.cdp.domain.service.DungeonSessionManager;
import fr.academy.cdp.domain.service.TestService;
import fr.academy.cdp.infrastructure.block.DungeonPortalBlock;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import fr.academy.cdp.infrastructure.command.CDPCommand;
import fr.academy.cdp.infrastructure.service.BattleScalingService;
import fr.academy.cdp.infrastructure.service.DungeonSurvivalService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class CDPMod implements ModInitializer {
    public static final String MOD_ID = "cdp";
    public static Block PORTAL_BLOCK;
    public static BlockEntityType<PortalBlockEntity> PORTAL_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        // Luminance configurée pour faire briller le portail même dans le noir
        PORTAL_BLOCK = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "dungeon_portal"),
                new DungeonPortalBlock(AbstractBlock.Settings.create()
                        .strength(-1.0f, 3600000.0f) // Incassable à la main
                        .nonOpaque()
                        .noCollision()
                        .luminance(state -> 13)));

        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dungeon_portal"),
                new BlockItem(PORTAL_BLOCK, new Item.Settings()));

        PORTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "portal_be"),
                BlockEntityType.Builder.create(PortalBlockEntity::new, PORTAL_BLOCK).build(null));

        CDPNetworking.registerPackets();
        BattleScalingService.register();
        DungeonSurvivalService.register();

        // LOGIQUE DE LOBBY
        ServerPlayNetworking.registerGlobalReceiver(CDPNetworking.ConfirmWarpPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var be = player.getWorld().getBlockEntity(payload.pos());
                var settings = (be instanceof PortalBlockEntity pbe) ? pbe.getSettings() : null;
                var session = DungeonSessionManager.getOrCreateSession(payload.pos(), settings);

                if (session.isActive()) {
                    player.sendMessage(Text.literal("§cCe portail est déjà verrouillé par une session active !"), true);
                    return;
                }

                if (session.addPlayer(player.getUuid())) {
                    syncLobby(session, context.server());
                }
            });
        });

        // LANCEMENT DU DONJON
        ServerPlayNetworking.registerGlobalReceiver(CDPNetworking.StartDungeonPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var session = DungeonSessionManager.getSession(payload.pos());
                if (session == null || !context.player().getUuid().equals(session.getLeader())) return;

                session.setActive(true);
                syncLobby(session, context.server());

                for (java.util.UUID uuid : session.getPlayers()) {
                    var member = context.server().getPlayerManager().getPlayer(uuid);
                    if (member != null) {
                        new TestService().executeTestSpawn(member, session.getSettings().levelCap());
                    }
                }
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new CDPCommand().register(dispatcher);
        });

        // SÉCURITÉ RESPRAWN
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            NbtList savedInv = DungeonSessionManager.loadInventory(newPlayer.getUuid());
            if (savedInv != null) {
                new TestService().forceCleanExit(newPlayer); // On restaure proprement
                newPlayer.sendMessage(Text.literal("§6[CDP] Ton état a été restauré après ta réapparition !"), false);
            }
        });

        // SÉCURITÉ CHANGEMENT DE DIMENSION (Sortie accidentelle)
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (origin.getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY) &&
                    !destination.getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) {
                player.getServer().execute(() -> new TestService().forceCleanExit(player));
            }
        });

        // SÉCURITÉ DÉCONNEXION
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            if (player.getWorld().getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) {
                new TestService().forceCleanExit(player);
            }
        });
    }

    private void syncLobby(DungeonSession session, net.minecraft.server.MinecraftServer server) {
        List<String> names = session.getPlayers().stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(java.util.Objects::nonNull)
                .map(p -> p.getNameForScoreboard())
                .toList();

        var updatePacket = new CDPNetworking.LobbyUpdatePayload(names, session.isActive());
        for (var player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, updatePacket);
        }
    }
}