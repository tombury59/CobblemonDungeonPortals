package fr.academy.cdp.client;

import fr.academy.cdp.CDPNetworking;
import fr.academy.cdp.infrastructure.ui.DungeonScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CDPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CDPNetworking.OpenScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // On passe le payload (les données du bloc) au constructeur de l'écran
                context.client().setScreen(new DungeonScreen(payload));
            });
        });
    }
}