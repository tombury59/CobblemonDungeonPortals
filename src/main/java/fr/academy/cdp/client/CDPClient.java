package fr.academy.cdp.client;

import fr.academy.cdp.CDPMod;
import fr.academy.cdp.CDPNetworking;
import fr.academy.cdp.infrastructure.ui.DungeonScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import fr.academy.cdp.infrastructure.client.PortalBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;


public class CDPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        BlockEntityRendererFactories.register(CDPMod.PORTAL_BLOCK_ENTITY, PortalBlockEntityRenderer::new);

        // 1. RÉCEPTEUR MANQUANT : Ouvrir l'UI quand le serveur le demande
        ClientPlayNetworking.registerGlobalReceiver(CDPNetworking.OpenScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Cette ligne transforme le paquet reçu en un écran visible !
                context.client().setScreen(new DungeonScreen(payload));
            });
        });

        // 2. RÉCEPTEUR EXISTANT : Mettre à jour la liste des pseudos
        ClientPlayNetworking.registerGlobalReceiver(CDPNetworking.LobbyUpdatePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof DungeonScreen screen) {
                    screen.updatePlayerList(payload.names(), payload.isStarted());
                }
            });
        });
    }
}