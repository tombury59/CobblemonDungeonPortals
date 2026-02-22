package fr.academy.cdp.infrastructure.client;

import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class PortalBlockEntityRenderer implements BlockEntityRenderer<PortalBlockEntity> {
    private static final Identifier BEAM_TEXTURE = Identifier.of("minecraft", "textures/entity/beacon_beam.png");

    public PortalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(PortalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var settings = entity.getSettings();
        if (settings == null) return;

        // 1. COULEUR SELON LE MODE (WAVE = Bleu, CLEAR = Vert)
        float r, g, b;
        if (settings.mode().equalsIgnoreCase("WAVE")) {
            r = 0.1f; g = 0.4f; b = 1.0f;
        } else {
            r = 0.1f; g = 1.0f; b = 0.2f;
        }

        // 2. LUMINOSITÉ INVERSÉE SELON LA DIFFICULTÉ
        // On part de 1.0 (très clair) et on soustrait selon la difficulté
        // EASY (0) -> factor 1.0 (Brillant/Blanc)
        // HARDCORE (4) -> factor 0.2 (Sombre/Profond)
        float factor = 1.0f - (settings.difficulty().ordinal() * 0.2f);

        // On s'assure que ça ne devienne pas totalement noir (minimum 0.1)
        factor = Math.max(0.1f, factor);

        int color = ColorHelper.Argb.getArgb(255, (int)(r * factor * 255), (int)(g * factor * 255), (int)(b * factor * 255));

        // 3. RENDU DU FAISCEAU
        BeaconBlockEntityRenderer.renderBeam(
                matrices,
                vertexConsumers,
                BEAM_TEXTURE,
                tickDelta,
                1.0f,
                entity.getWorld().getTime(),
                0,
                256,
                color,
                0.2f,
                0.35f
        );
    }
}