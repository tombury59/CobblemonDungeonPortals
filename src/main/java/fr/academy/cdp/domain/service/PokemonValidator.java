package fr.academy.cdp.domain.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;

public class PokemonValidator {
    public static List<Integer> getPlayerPokemonLevels(ServerPlayerEntity player) {
        List<Integer> levels = new ArrayList<>();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : party) {
            levels.add(pokemon.getLevel());
        }
        return levels;
    }
}