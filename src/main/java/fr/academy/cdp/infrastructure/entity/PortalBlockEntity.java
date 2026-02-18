package fr.academy.cdp.infrastructure.entity;

import fr.academy.cdp.CDPMod;
import fr.academy.cdp.domain.model.DungeonDifficulty;
import fr.academy.cdp.domain.model.PortalSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class PortalBlockEntity extends BlockEntity {
    private PortalSettings settings;

    public PortalBlockEntity(BlockPos pos, BlockState state) {
        super(CDPMod.PORTAL_BLOCK_ENTITY, pos, state);
        this.settings = PortalSettings.generateRandom();
    }

    public PortalSettings getSettings() { return settings; }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("Cap", settings.levelCap());
        nbt.putString("Mode", settings.mode());
        nbt.putInt("Diff", settings.difficulty().ordinal());
        nbt.putString("T1", settings.type1());
        if (settings.type2() != null) nbt.putString("T2", settings.type2());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.settings = new PortalSettings(
                nbt.getInt("Cap"),
                nbt.getString("Mode"),
                DungeonDifficulty.values()[nbt.getInt("Diff")],
                nbt.getString("T1"),
                nbt.contains("T2") ? nbt.getString("T2") : null
        );
    }
}