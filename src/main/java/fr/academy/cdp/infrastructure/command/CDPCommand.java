package fr.academy.cdp.infrastructure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.academy.cdp.CDPMod;
import fr.academy.cdp.domain.model.DungeonDifficulty;
import fr.academy.cdp.domain.model.PortalSettings;
import fr.academy.cdp.domain.service.DungeonSessionManager;
import fr.academy.cdp.domain.service.TestService;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CDPCommand {
    private final TestService testService = new TestService();

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cdp")

                // --- 1. SPAWN (Admin) ---
                .then(CommandManager.literal("spawn")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> spawnPortal(context.getSource(), null))
                        .then(CommandManager.argument("cap", IntegerArgumentType.integer(20, 100))
                                .then(CommandManager.argument("difficulty", StringArgumentType.string())
                                        .then(CommandManager.argument("mode", StringArgumentType.string())
                                                .executes(context -> {
                                                    int cap = IntegerArgumentType.getInteger(context, "cap");
                                                    String diffStr = StringArgumentType.getString(context, "difficulty").toUpperCase();
                                                    String mode = StringArgumentType.getString(context, "mode").toUpperCase();

                                                    DungeonDifficulty diff;
                                                    try { diff = DungeonDifficulty.valueOf(diffStr); }
                                                    catch (Exception e) { diff = DungeonDifficulty.NORMAL; }

                                                    PortalSettings custom = new PortalSettings(cap, mode, diff, "Fire", "Dragon");
                                                    return spawnPortal(context.getSource(), custom);
                                                }))))
                )

                // --- 2. LEAVE / QUIT (Joueur) ---
                .then(CommandManager.literal("leave")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            // Si le joueur est dans le monde du donjon, on le renvoie à l'Overworld
                            if (player.getWorld().getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) {
                                testService.teleportBack(player);
                                return 1;
                            }

                            // Sinon, on vérifie s'il est dans un lobby de portail
                            var session = DungeonSessionManager.getPlayerSession(player.getUuid());
                            if (session != null) {
                                session.getPlayers().remove(player.getUuid());
                                player.sendMessage(Text.literal("§6[CDP] Vous avez quitté le lobby."), false);
                                return 1;
                            }

                            player.sendMessage(Text.literal("§cVous n'êtes ni en donjon, ni en lobby."), false);
                            return 0;
                        })
                )

                // --- 3. TEST (Debug) ---
                .then(CommandManager.literal("test")
                        .executes(context -> {
                            var player = context.getSource().getPlayer();
                            if (player != null) {
                                testService.executeTestSpawn(player, 20);
                                return 1;
                            }
                            return 0;
                        })
                )
        );
    }

    private int spawnPortal(ServerCommandSource source, PortalSettings customSettings) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrThrow();
        var world = player.getWorld();
        BlockPos pos = player.getBlockPos().offset(player.getHorizontalFacing(), 2);

        PortalSettings settings = (customSettings != null) ? customSettings : PortalSettings.generateRandom();
        world.setBlockState(pos, CDPMod.PORTAL_BLOCK.getDefaultState());

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PortalBlockEntity be) {
            be.setSettings(settings);
            source.sendFeedback(() -> Text.literal("§b[CDP] §aPortail invoqué ! §7(" +
                    settings.mode() + " | Cap " + settings.levelCap() + " | " + settings.difficulty() + ")"), false);
            return 1;
        }
        return 0;
    }
}