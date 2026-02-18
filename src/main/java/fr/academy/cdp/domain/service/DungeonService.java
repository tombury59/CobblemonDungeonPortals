package fr.academy.cdp.domain.service;

import fr.academy.cdp.CDPNetworking;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class DungeonService {
    public void interactWithPortal(ServerPlayerEntity player, BlockPos pos) {
        var be = player.getWorld().getBlockEntity(pos);
        if (be instanceof PortalBlockEntity portalBe) {
            var s = portalBe.getSettings();
            // On envoie les données réelles du bloc au joueur
            ServerPlayNetworking.send(player, new CDPNetworking.OpenScreenPayload(
                    s.levelCap(), s.mode(), s.difficulty().ordinal(), s.type1(), s.type2() != null ? s.type2() : ""
            ));
        }
    }
}