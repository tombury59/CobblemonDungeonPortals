package fr.academy.cdp;

// AJOUTE CET IMPORT :
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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.UUID;

public class CDPMod implements ModInitializer {
    public static final String MOD_ID = "cdp";
    public static Block PORTAL_BLOCK;
    public static BlockEntityType<PortalBlockEntity> PORTAL_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        PORTAL_BLOCK = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "dungeon_portal"),
                new DungeonPortalBlock(AbstractBlock.Settings.create().strength(4.0f).nonOpaque()));

        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dungeon_portal"),
                new BlockItem(PORTAL_BLOCK, new Item.Settings()));

        PORTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "portal_be"),
                BlockEntityType.Builder.create(PortalBlockEntity::new, PORTAL_BLOCK).build(null));

        CDPNetworking.registerPackets();
        BattleScalingService.register();
        DungeonSurvivalService.register();

        ServerPlayNetworking.registerGlobalReceiver(CDPNetworking.ConfirmWarpPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var be = player.getWorld().getBlockEntity(payload.pos());
                var settings = (be instanceof PortalBlockEntity pbe) ? pbe.getSettings() : null;
                var session = DungeonSessionManager.getOrCreateSession(payload.pos(), settings);

                // SÉCURITÉ : Empêcher de rejoindre si c'est déjà actif
                if (session.isActive()) {
                    player.sendMessage(Text.literal("§cLe donjon a déjà commencé !"), true);
                    return;
                }

                if (session.addPlayer(player.getUuid())) {
                    syncLobby(session, context.server());
                } else {
                    player.sendMessage(Text.literal("§cLe lobby est plein !"), true);
                }
            });
        });

// Dans le récepteur StartDungeonPayload de CDPMod.java
        ServerPlayNetworking.registerGlobalReceiver(CDPNetworking.StartDungeonPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var session = DungeonSessionManager.getSession(payload.pos());
                if (session == null || !context.player().getUuid().equals(session.getLeader())) return;

                // MARQUER ACTIF (Verrouille le portail)
                session.setActive(true);
                syncLobby(session, context.server()); // On prévient tout le monde que c'est lancé

                int cap = session.getSettings().levelCap();

                for (java.util.UUID uuid : session.getPlayers()) {
                    var groupMember = context.server().getPlayerManager().getPlayer(uuid);
                    if (groupMember != null) {
                        groupMember.sendMessage(Text.literal("§6Le donjon commence !"), false);
                        new TestService().executeTestSpawn(groupMember, cap);
                    }
                }
                // Ne SURTOUT PAS faire closeSession ici, sinon la session disparaît
                // On la supprimera dans TestService.teleportBack quand le dernier joueur sort.
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new CDPCommand().register(dispatcher);
        });
    }

    private void syncLobby(DungeonSession session, net.minecraft.server.MinecraftServer server) {
        List<String> names = session.getPlayers().stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(java.util.Objects::nonNull)
                .map(p -> p.getNameForScoreboard())
                .toList();

        // On envoie la liste ET l'état "active"
        var updatePacket = new CDPNetworking.LobbyUpdatePayload(names, session.isActive());

        // On envoie à tout le monde sur le serveur pour que même ceux qui n'ont pas encore
        // rejoint voient l'état changer s'ils ouvrent l'interface
        for (var player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, updatePacket);
        }
    }
}