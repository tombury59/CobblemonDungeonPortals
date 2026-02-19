package fr.academy.cdp.infrastructure.ui;

import fr.academy.cdp.CDPNetworking;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import java.util.List;

public class DungeonScreen extends BaseUIModelScreen<FlowLayout> {
    private final CDPNetworking.OpenScreenPayload payload;

    public DungeonScreen(CDPNetworking.OpenScreenPayload payload) {
        super(FlowLayout.class, DataSource.asset(Identifier.of("cdp", "dungeon_portal")));
        this.payload = payload;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var modeLabel = rootComponent.childById(LabelComponent.class, "mode-label");
        var capLabel = rootComponent.childById(LabelComponent.class, "cap-label");
        var type1Label = rootComponent.childById(LabelComponent.class, "type-1-label");
        var type2Label = rootComponent.childById(LabelComponent.class, "type-2-label");
        var warpBtn = rootComponent.childById(ButtonComponent.class, "warp-button");

        // Injection des données de base du portail
        if (modeLabel != null) modeLabel.text(Text.literal("§7MODE: §f" + payload.mode().toUpperCase()));
        if (capLabel != null) capLabel.text(Text.literal("§7LIMIT: §eLvl." + payload.cap()));

        if (type1Label != null) {
            type1Label.text(Text.literal("§6[" + payload.t1().toUpperCase() + "]"));
        }

        if (type2Label != null && !payload.t2().isEmpty()) {
            type2Label.text(Text.literal("§e[" + payload.t2().toUpperCase() + "]"));
        } else if (type2Label != null) {
            type2Label.text(Text.literal(""));
        }

        // Configuration initiale du bouton (état par défaut)
        if (warpBtn != null) {
            warpBtn.setMessage(Text.literal("§6⚡ CRÉER L'INSTANCE"));
            warpBtn.onPress(button -> {
                ClientPlayNetworking.send(new CDPNetworking.ConfirmWarpPayload(payload.pos()));
                button.active(false); // Désactive pour éviter le double-clic avant l'update
            });
        }
    }

    /**
     * Appelé quand le serveur envoie la liste des joueurs (LobbyUpdatePayload)
     */
    public void updatePlayerList(List<String> names) {
        if (this.uiAdapter == null) return;
        var root = this.uiAdapter.rootComponent;

        var label = root.childById(LabelComponent.class, "no-players-label");
        var warpBtn = root.childById(ButtonComponent.class, "warp-button");

        // 1. Mise à jour de la liste des noms dans le cadre noir
        if (label != null) {
            if (names.isEmpty()) {
                label.text(Text.literal("§8Empty..."));
            } else {
                StringBuilder sb = new StringBuilder();
                for (String name : names) {
                    sb.append("§f• ").append(name).append("\n");
                }
                label.text(Text.literal(sb.toString()));
            }
        }

        // 2. Logique de changement d'état du bouton
        if (warpBtn != null && MinecraftClient.getInstance().player != null) {
            String localPlayer = MinecraftClient.getInstance().player.getNameForScoreboard();

            // CAS : Le joueur est déjà inscrit dans l'instance
            if (names.contains(localPlayer)) {
                if (names.get(0).equals(localPlayer)) {
                    // C'est le CHEF
                    warpBtn.active(true);
                    warpBtn.setMessage(Text.literal("§b§l▶ LANCER L'INSTANCE"));
                    warpBtn.onPress(button -> {
                        ClientPlayNetworking.send(new CDPNetworking.StartDungeonPayload(payload.pos()));
                        this.close();
                    });
                } else {
                    // C'est un MEMBRE
                    warpBtn.active(false);
                    warpBtn.setMessage(Text.literal("§7EN ATTENTE DU CHEF..."));
                }
            }
            // CAS : Une instance existe déjà mais le joueur n'est pas dedans
            else if (!names.isEmpty()) {
                warpBtn.active(true);
                warpBtn.setMessage(Text.literal("§a✚ REJOINDRE L'INSTANCE"));
                warpBtn.onPress(button -> {
                    ClientPlayNetworking.send(new CDPNetworking.ConfirmWarpPayload(payload.pos()));
                    button.active(false);
                });
            }
            // CAS : Personne n'est inscrit (retour à l'état initial si tout le monde quitte)
            else {
                warpBtn.active(true);
                warpBtn.setMessage(Text.literal("§6⚡ CRÉER L'INSTANCE"));
                warpBtn.onPress(button -> {
                    ClientPlayNetworking.send(new CDPNetworking.ConfirmWarpPayload(payload.pos()));
                    button.active(false);
                });
            }
        }
    }
}