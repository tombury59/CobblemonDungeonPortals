package fr.academy.cdp.infrastructure.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.academy.cdp.domain.service.TestService;
import kotlin.Unit;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class DungeonSurvivalService {

    public static void register() {
        // --- 1. RÈGLE : DÉFAITE SI TOUTE L'ÉQUIPE EST KO ---
        CobblemonEvents.INSTANCE.POKEMON_FAINTED.subscribe(Priority.NORMAL, (event) -> {
            var owner = event.getPokemon().getOwnerPlayer();
            // On s'assure que c'est bien un joueur qui possède le Pokémon tombé
            if (owner instanceof ServerPlayerEntity player) {
                checkPlayerSurvival(player);
            }
            return Unit.INSTANCE;
        });

        // --- 2. RÈGLE : INTERDICTION DE FRAPPER LES POKÉMON ---
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) {
                // On vérifie si c'est une entité de Cobblemon
                if (entity instanceof PokemonEntity) {
                    if (!player.isCreative()) {
                        // Annulation stricte côté serveur
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    /**
     * Vérifie si le joueur a encore des Pokémon aptes au combat.
     * Si non, déclenche la téléportation de retour (défaite).
     */
    public static void checkPlayerSurvival(ServerPlayerEntity player) {
        // Sécurité : ne fonctionne que dans le monde du donjon
        if (!player.getWorld().getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) return;

        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        boolean hasAlive = false;

        // On parcourt l'équipe pour chercher au moins un Pokémon avec des PV
        for (Pokemon p : party) {
            if (p.getCurrentHealth() > 0) {
                hasAlive = true;
                break;
            }
        }

        // Si l'équipe est totalement KO
        if (!hasAlive) {
            player.sendMessage(Text.literal("§c[DÉFAITE] Toute votre équipe est hors de combat !"), false);
            // On instancie le service pour renvoyer le joueur au spawn et restaurer son inventaire
            new TestService().teleportBack(player);
        }
    }
}