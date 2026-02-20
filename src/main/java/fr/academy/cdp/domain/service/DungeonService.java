package fr.academy.cdp.domain.service;

import fr.academy.cdp.CDPNetworking;
import fr.academy.cdp.domain.model.DungeonSession;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class DungeonService {
    public void interactWithPortal(ServerPlayerEntity player, BlockPos pos) {
        var be = player.getWorld().getBlockEntity(pos);
        if (be instanceof PortalBlockEntity portalBe) {
            // 1. On récupère les settings
            var settings = portalBe.getSettings();

            // 2. On récupère ou crée la session
            var session = DungeonSessionManager.getOrCreateSession(pos, settings);

            // 3. On prépare la liste des noms
            List<String> names = session.getPlayers().stream()
                    .map(uuid -> player.getServer().getPlayerManager().getPlayer(uuid))
                    .filter(p -> p != null)
                    .map(p -> p.getNameForScoreboard())
                    .toList();

            // 4. On envoie l'OpenScreenPayload
            // CORRECTION FINALE : On utilise les noms exacts de ton record
            ServerPlayNetworking.send(player, new CDPNetworking.OpenScreenPayload(
                    settings.levelCap(),    // au lieu de cap()
                    settings.mode(),        // inchangé
                    settings.difficulty().ordinal(), // difficulty est une Enum, on envoie son index (int)
                    settings.type1(),       // au lieu de t1()
                    settings.type2(),       // au lieu de t2()
                    pos,
                    names,
                    session.isActive()
            ));

            // 5. On envoie l'update de lobby
            ServerPlayNetworking.send(player, new CDPNetworking.LobbyUpdatePayload(names, session.isActive()));
        }
    }
}