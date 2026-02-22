package fr.academy.cdp.infrastructure.service;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.util.Optional;

public class StructureService {

    public static void placeDungeonStructure(ServerWorld world, BlockPos pos, String structureName) {
        StructureTemplateManager manager = world.getServer().getStructureTemplateManager();
        // Charge le fichier .nbt depuis ton mod
        Optional<StructureTemplate> template = manager.getTemplate(Identifier.of("cdp", structureName));

        template.ifPresent(t -> {
            StructurePlacementData data = new StructurePlacementData();
            // On colle la structure au point d'ancrage (pos)
            t.place(world, pos, pos, data, world.random, 2);
        });
    }
}