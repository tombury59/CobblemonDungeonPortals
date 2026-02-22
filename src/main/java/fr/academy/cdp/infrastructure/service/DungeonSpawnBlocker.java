package fr.academy.cdp.infrastructure.service;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.entity.event.v1.EntitySpawnCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import fr.academy.cdp.domain.service.TestService;

public class DungeonSpawnBlocker {
    public static void register() {
        EntitySpawnCallback.EVENT.register((entity, world) -> {
            if (world instanceof ServerWorld sw && sw.getRegistryKey().equals(TestService.DUNGEON_WORLD_KEY)) {
                if (entity instanceof PokemonEntity) {
                    return EntitySpawnCallback.Result.FAIL;
                }
            }
            return EntitySpawnCallback.Result.SUCCESS;
        });
    }
}
