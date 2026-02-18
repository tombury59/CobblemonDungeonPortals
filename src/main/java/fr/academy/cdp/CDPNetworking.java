package fr.academy.cdp;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class CDPNetworking {
    public static final Identifier OPEN_SCREEN_ID = Identifier.of("cdp", "open_dungeon_screen");

    public record OpenScreenPayload(int cap, String mode, int diff, String t1, String t2) implements CustomPayload {
        public static final Id<OpenScreenPayload> ID = new Id<>(OPEN_SCREEN_ID);

        public static final PacketCodec<RegistryByteBuf, OpenScreenPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, OpenScreenPayload::cap,
                PacketCodecs.STRING, OpenScreenPayload::mode,
                PacketCodecs.VAR_INT, OpenScreenPayload::diff,
                PacketCodecs.STRING, OpenScreenPayload::t1,
                PacketCodecs.STRING, OpenScreenPayload::t2,
                OpenScreenPayload::new
        );

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
    }
}