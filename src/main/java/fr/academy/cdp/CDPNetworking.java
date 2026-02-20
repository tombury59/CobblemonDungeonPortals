package fr.academy.cdp;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class CDPNetworking {
    public static final Identifier OPEN_SCREEN_ID = Identifier.of("cdp", "open_screen");
    public static final Identifier CONFIRM_WARP_ID = Identifier.of("cdp", "confirm_warp");
    public static final Identifier LOBBY_UPDATE_ID = Identifier.of("cdp", "lobby_update");
    public static final Identifier START_DUNGEON_ID = Identifier.of("cdp", "start_dungeon");

    public record OpenScreenPayload(int cap, String mode, int diff, String t1, String t2, BlockPos pos, List<String> currentNames, boolean isStarted) implements CustomPayload {
        public static final Id<OpenScreenPayload> ID = new Id<>(Identifier.of("cdp", "open_screen"));

        public static final PacketCodec<RegistryByteBuf, OpenScreenPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeVarInt(value.cap);
                    buf.writeString(value.mode);
                    buf.writeVarInt(value.diff);
                    buf.writeString(value.t1);
                    buf.writeString(value.t2);
                    buf.writeBlockPos(value.pos);
                    // Utilisation simplifiée du codec de liste
                    PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, value.currentNames);
                    buf.writeBoolean(value.isStarted);
                },
                buf -> new OpenScreenPayload(
                        buf.readVarInt(),
                        buf.readString(),
                        buf.readVarInt(),
                        buf.readString(),
                        buf.readString(),
                        buf.readBlockPos(),
                        PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf),
                        buf.readBoolean()
                )
        );

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }


    public record ConfirmWarpPayload(BlockPos pos) implements CustomPayload {
        public static final Id<ConfirmWarpPayload> ID = new Id<>(CONFIRM_WARP_ID);
        public static final PacketCodec<RegistryByteBuf, ConfirmWarpPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, ConfirmWarpPayload::pos,
                ConfirmWarpPayload::new
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // MODIFIÉ : Ajout de isStarted pour mettre à jour le bouton en temps réel pour les joueurs déjà sur l'écran
    public record LobbyUpdatePayload(List<String> names, boolean isStarted) implements CustomPayload {
        public static final Id<LobbyUpdatePayload> ID = new Id<>(LOBBY_UPDATE_ID);
        public static final PacketCodec<RegistryByteBuf, LobbyUpdatePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING.collect(PacketCodecs.toList()), LobbyUpdatePayload::names,
                PacketCodecs.BOOL, LobbyUpdatePayload::isStarted,
                LobbyUpdatePayload::new
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record StartDungeonPayload(BlockPos pos) implements CustomPayload {
        public static final Id<StartDungeonPayload> ID = new Id<>(START_DUNGEON_ID);
        public static final PacketCodec<RegistryByteBuf, StartDungeonPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, StartDungeonPayload::pos,
                StartDungeonPayload::new
        );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LobbyUpdatePayload.ID, LobbyUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmWarpPayload.ID, ConfirmWarpPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StartDungeonPayload.ID, StartDungeonPayload.CODEC);
    }
}