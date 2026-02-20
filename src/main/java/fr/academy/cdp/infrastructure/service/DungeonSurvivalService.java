package fr.academy.cdp.infrastructure.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.domain.service.TestService;
import kotlin.Unit;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DungeonSurvivalService {

    public static void register() {
        // En Java, on accède souvent au champ directement s'il est public en Kotlin
        CobblemonEvents.INSTANCE.POKEMON_FAINTED.subscribe(Priority.NORMAL, (event) -> {
            // On récupère le dresseur du Pokémon qui vient de tomber KO
            var owner = event.getPokemon().getOwnerPlayer();

            if (owner instanceof ServerPlayerEntity player) {
                checkPlayerSurvival(player);
            }
            return Unit.INSTANCE;
        });
    }

    public static void checkPlayerSurvival(ServerPlayerEntity player) {
        if (!player.getWorld().getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) return;

        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        boolean hasAlive = false;

        for (Pokemon p : party) {
            if (p.getCurrentHealth() > 0) {
                hasAlive = true;
                break;
            }
        }

        if (!hasAlive) {
            player.sendMessage(Text.literal("§c[DÉFAITE] Votre équipe est KO !"), false);
            new TestService().teleportBack(player);
        }
    }
}