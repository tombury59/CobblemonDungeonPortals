package fr.academy.cdp.infrastructure.entity;

import fr.academy.cdp.CDPMod;
import fr.academy.cdp.domain.model.DungeonDifficulty;
import fr.academy.cdp.domain.model.PortalSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PortalBlockEntity extends BlockEntity {
    private PortalSettings settings = PortalSettings.generateRandom();

    public PortalBlockEntity(BlockPos pos, BlockState state) {
        super(CDPMod.PORTAL_BLOCK_ENTITY, pos, state);
    }

    public void setSettings(PortalSettings settings) {
        this.settings = settings;
        this.markDirty();
        if (world != null && !world.isClient) {
            // Envoie les nouvelles couleurs à tous les joueurs proches
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public PortalSettings getSettings() {
        return settings;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("LevelCap", settings.levelCap());
        nbt.putString("Mode", settings.mode());
        nbt.putString("Difficulty", settings.difficulty().name());
        nbt.putString("Type1", settings.type1());
        nbt.putString("Type2", settings.type2());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.settings = new PortalSettings(
                nbt.getInt("LevelCap"),
                nbt.getString("Mode"),
                DungeonDifficulty.valueOf(nbt.getString("Difficulty")),
                nbt.getString("Type1"),
                nbt.getString("Type2")
        );
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}