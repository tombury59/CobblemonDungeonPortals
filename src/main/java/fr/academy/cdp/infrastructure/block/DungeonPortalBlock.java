package fr.academy.cdp.infrastructure.block;

import com.mojang.serialization.MapCodec;
import fr.academy.cdp.domain.service.DungeonService;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DungeonPortalBlock extends BlockWithEntity {
    public static final MapCodec<DungeonPortalBlock> CODEC = createCodec(DungeonPortalBlock::new);
    // On instancie le service qui gère l'ouverture
    private final DungeonService dungeonService = new DungeonService();

    public DungeonPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // On ne fait rien côté client (on attend que le serveur donne l'ordre)
        if (world.isClient) return ActionResult.SUCCESS;

        // Si c'est le serveur, on appelle notre service
        if (player instanceof ServerPlayerEntity serverPlayer) {
            dungeonService.interactWithPortal(serverPlayer, pos);
        }

        return ActionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PortalBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}