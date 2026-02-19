package fr.academy.cdp;

// AJOUTE CET IMPORT :
import fr.academy.cdp.domain.model.DungeonSession;
import fr.academy.cdp.domain.service.DungeonSessionManager;
import fr.academy.cdp.domain.service.TestService;
import fr.academy.cdp.infrastructure.block.DungeonPortalBlock;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import fr.academy.cdp.infrastructure.command.CDPCommand;
import fr.academy.cdp.infrastructure.service.BattleScalingService;
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

        ServerPlayNetworking.registerGlobalReceiver(CDPNetworking.ConfirmWarpPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var be = player.getWorld().getBlockEntity(payload.pos());
                var settings = (be instanceof PortalBlockEntity pbe) ? pbe.getSettings() : null;
                var session = DungeonSessionManager.getOrCreateSession(payload.pos(), settings);

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

                int cap = session.getSettings().levelCap(); // On récupère le niveau du donjon

                for (java.util.UUID uuid : session.getPlayers()) {
                    var groupMember = context.server().getPlayerManager().getPlayer(uuid);
                    if (groupMember != null) {
                        groupMember.sendMessage(net.minecraft.text.Text.literal("§6Le donjon commence !"), false);

                        // On passe le CAP ici pour brider les pokémon
                        new TestService().executeTestSpawn(groupMember, cap);
                    }
                }
                DungeonSessionManager.closeSession(payload.pos());
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new CDPCommand().register(dispatcher);
        });
    }

    private void syncLobby(DungeonSession session, net.minecraft.server.MinecraftServer server) {
        List<String> names = session.getPlayers().stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(p -> p != null)
                .map(p -> p.getNameForScoreboard())
                .toList();
        var updatePacket = new CDPNetworking.LobbyUpdatePayload(names);
        for (UUID pUuid : session.getPlayers()) {
            var target = server.getPlayerManager().getPlayer(pUuid);
            if (target != null) ServerPlayNetworking.send(target, updatePacket);
        }
    }
}