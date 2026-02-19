package fr.academy.cdp.infrastructure.service;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.domain.service.DungeonSessionManager;
import fr.academy.cdp.domain.model.DungeonSession;
import kotlin.Unit;
import java.util.Arrays;
import java.util.UUID;

public class BattleScalingService {
    public static void register() {
        CobblemonEvents.INSTANCE.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, (event) -> {
            var battle = event.getBattle();
            battle.getSides().forEach(side -> {
                Arrays.stream(side.getActors()).forEach(actor -> {
                    UUID playerUuid = actor.getUuid();
                    if (playerUuid != null) {
                        DungeonSession session = DungeonSessionManager.getPlayerSession(playerUuid);
                        if (session != null) {
                            int cap = session.getSettings().levelCap();
                            actor.getPokemonList().forEach(bp -> {
                                Pokemon p = bp.getEffectedPokemon();
                                if (p.getLevel() > cap) {
                                    p.setLevel(cap);
                                    // p.reloadStats() n'existe pas, on laisse Cobblemon gérer les stats
                                    // Le simple fait de setLevel met à jour les stats du combat
                                }
                            });
                        }
                    }
                });
            });
            return Unit.INSTANCE;
        });
    }
}