package fr.academy.cdp.domain.service;

import fr.academy.cdp.CDPNetworking;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class DungeonService {
    public void interactWithPortal(ServerPlayerEntity player, BlockPos pos) {
        var be = player.getWorld().getBlockEntity(pos);
        if (be instanceof PortalBlockEntity portalBe) {
            var s = portalBe.getSettings();
            var session = DungeonSessionManager.getSession(pos);

            // Correction : On utilise type1() et type2() car c'est ce qu'il y a dans ton record PortalSettings
            ServerPlayNetworking.send(player, new CDPNetworking.OpenScreenPayload(
                    s.levelCap(), s.mode(), s.difficulty().ordinal(), s.type1(), s.type2() != null ? s.type2() : "", pos
            ));

            if (session != null) {
                List<String> names = session.getPlayers().stream()
                        .map(uuid -> player.getServer().getPlayerManager().getPlayer(uuid))
                        .filter(p -> p != null)
                        .map(p -> p.getNameForScoreboard())
                        .toList();
                ServerPlayNetworking.send(player, new CDPNetworking.LobbyUpdatePayload(names));
            }
        }
    }
}