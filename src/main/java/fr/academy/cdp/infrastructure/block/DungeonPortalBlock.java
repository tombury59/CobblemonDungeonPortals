package fr.academy.cdp.infrastructure.block;

import com.mojang.serialization.MapCodec;
import fr.academy.cdp.domain.model.PortalSettings;
import fr.academy.cdp.domain.service.DungeonService;
import fr.academy.cdp.domain.service.PortalRegistry; // Import de ton registre
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity; // <--- MANQUANT
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack; // <--- MANQUANT
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld; // <--- MANQUANT (pour le cast du monde)
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class DungeonPortalBlock extends BlockWithEntity {
    public static final MapCodec<DungeonPortalBlock> CODEC = createCodec(DungeonPortalBlock::new);
    private final DungeonService dungeonService = new DungeonService();

    public DungeonPortalBlock(Settings settings) { super(settings); }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PortalBlockEntity portalBE) || portalBE.getSettings() == null) return;

        var settings = portalBE.getSettings();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // --- COULEUR DES PARTICULES (Logique Inversée) ---
        Vector3f colorVec = settings.mode().equalsIgnoreCase("WAVE")
                ? new Vector3f(0.1f, 0.4f, 1.0f)
                : new Vector3f(0.1f, 1.0f, 0.2f);

        float factor = 1.0f - (settings.difficulty().ordinal() * 0.2f);
        factor = Math.max(0.2f, factor); // Minimum de visibilité pour les particules
        colorVec.mul(factor);

        // Vortex
        for (int i = 0; i < 3; i++) {
            double angle = (world.getTime() * 0.1) + (i * Math.PI / 1.5);
            double px = cx + Math.cos(angle) * 0.5;
            double pz = cz + Math.sin(angle) * 0.5;
            world.addParticle(new DustParticleEffect(colorVec, 1.2f), px, cy + (random.nextDouble() * 2.0), pz, 0, 0.05, 0);
        }

        // Ajout de fumée blanche si c'est EASY (pour accentuer l'effet "clair")
        if (settings.difficulty().ordinal() == 0 && random.nextInt(2) == 0) {
            world.addParticle(ParticleTypes.CLOUD, cx, cy + 0.5, cz, 0, 0.01, 0);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (player instanceof ServerPlayerEntity serverPlayer) dungeonService.interactWithPortal(serverPlayer, pos);
        return ActionResult.CONSUME;
    }

    @Nullable @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new PortalBlockEntity(pos, state); }
    @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.INVISIBLE; }

    //@Override
    //public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    //    if (!world.isClient && placer instanceof PlayerEntity player) {
    //        // On enregistre le nouveau portail et on supprime l'ancien du joueur
    //        PortalRegistry.registerPortal(player.getUuid(), (ServerWorld) world, pos);
//
    //        // Petit message d'info (optionnel)
    //        player.sendMessage(net.minecraft.text.Text.literal("§e[CDP] Ton ancien portail a été déplacé ici."), true);
    //    }
    //    super.onPlaced(world, pos, state, placer, itemStack);
    //}
}