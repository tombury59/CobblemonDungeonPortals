package fr.academy.cdp.infrastructure.command;

import com.mojang.brigadier.CommandDispatcher;
import fr.academy.cdp.domain.service.TestService;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CDPCommand {
    private final TestService testService = new TestService();

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cdp")
                .then(CommandManager.literal("test")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getPlayer() != null) {
                                testService.executeTestSpawn(source.getPlayer());                                source.sendFeedback(() -> Text.literal("§b[CDP] Bloc de test invoqué !"), false);
                                return 1;
                            }
                            return 0;
                        })
                )
        );
    }
}