package fr.academy.cdp;

import fr.academy.cdp.infrastructure.block.DungeonPortalBlock;
import fr.academy.cdp.infrastructure.entity.PortalBlockEntity;
import fr.academy.cdp.infrastructure.command.CDPCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CDPMod implements ModInitializer {
    public static final String MOD_ID = "cdp";

    // On déclare les variables, mais on ne les instancie pas tout de suite ici
    public static Block PORTAL_BLOCK;
    public static BlockEntityType<PortalBlockEntity> PORTAL_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        // 1. Enregistrer le BLOC
        PORTAL_BLOCK = Registry.register(
                Registries.BLOCK,
                Identifier.of(MOD_ID, "dungeon_portal"),
                new DungeonPortalBlock(AbstractBlock.Settings.create().strength(4.0f).nonOpaque())
        );

        // 2. Enregistrer l'ITEM du bloc (pour l'avoir dans l'inventaire)
        Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "dungeon_portal"),
                new BlockItem(PORTAL_BLOCK, new Item.Settings())
        );

        // 3. Enregistrer la BLOCK ENTITY
        PORTAL_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MOD_ID, "portal_be"),
                BlockEntityType.Builder.create(PortalBlockEntity::new, PORTAL_BLOCK).build(null)
        );

        // 4. Réseau et Commandes
        CDPNetworking.registerPackets();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new CDPCommand().register(dispatcher);
        });
    }
}