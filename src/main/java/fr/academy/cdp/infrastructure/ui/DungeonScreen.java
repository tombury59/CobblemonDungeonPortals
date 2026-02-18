package fr.academy.cdp.infrastructure.ui;

import fr.academy.cdp.CDPNetworking;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.util.Identifier;

public class DungeonScreen extends BaseUIModelScreen<FlowLayout> {
    private final CDPNetworking.OpenScreenPayload payload;

    public DungeonScreen(CDPNetworking.OpenScreenPayload payload) {
        // On lie le fichier XML ici
        super(FlowLayout.class, DataSource.asset(Identifier.of("cdp", "dungeon_portal")));
        this.payload = payload;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        // On récupère les composants par leur ID défini dans le XML
        var title = rootComponent.childById(LabelComponent.class, "title");
        var modeLabel = rootComponent.childById(LabelComponent.class, "mode-label");
        var capLabel = rootComponent.childById(LabelComponent.class, "cap-label");
        var warpBtn = rootComponent.childById(ButtonComponent.class, "warp-button");

        // On injecte les données
        if (modeLabel != null) modeLabel.text(net.minecraft.text.Text.literal("§7MODE: §f" + payload.mode()));
        if (capLabel != null) capLabel.text(net.minecraft.text.Text.literal("§7LIMIT: §eLvl." + payload.cap()));

        if (warpBtn != null) {
            warpBtn.onPress(button -> {
                // Ici on lancera la téléportation
                this.close();
            });
        }

    }
}