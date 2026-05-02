package com.jsaperr.atom;

import com.jsaperr.atom.block.MorphPlatformBlock;
import com.jsaperr.atom.block.MorphRequestPayload;
import com.jsaperr.atom.block.MorphSelectPayload;
import com.jsaperr.atom.block.ResearchStationBlock;
import com.jsaperr.atom.block.ResearchStationBlockEntity;
import com.jsaperr.atom.block.ResearchSyncPayload;
import com.jsaperr.atom.block.gui.MorphPlatformMenu;
import com.jsaperr.atom.block.gui.MorphPlatformScreen;
import com.jsaperr.atom.block.gui.ResearchStationMenu;
import com.jsaperr.atom.block.gui.ResearchStationScreen;
import com.jsaperr.atom.item.ExtractorItem;
import com.jsaperr.atom.morph.ChickenFlutterPayload;
import com.jsaperr.atom.morph.MorphAttachments;
import com.jsaperr.atom.morph.MorphEventHandler;
import com.jsaperr.atom.morph.MorphSyncPayload;
import com.jsaperr.atom.morph.MorphUnlockAttachments;
import com.jsaperr.atom.morph.passive.MorphPassiveHandler;
import com.jsaperr.atom.shell.ShellAttachments;
import com.jsaperr.atom.shell.ShellEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(Atom.MODID)
public class Atom {
    public static final String MODID = "atom";

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final Supplier<ResearchStationBlock> RESEARCH_STATION =
            BLOCKS.register("research_station", () -> new ResearchStationBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            ));

    public static final Supplier<BlockEntityType<ResearchStationBlockEntity>> RESEARCH_STATION_BE =
            BLOCK_ENTITY_TYPES.register("research_station", () ->
                    BlockEntityType.Builder.of(ResearchStationBlockEntity::new, RESEARCH_STATION.get()).build(null));

    public static final Supplier<ExtractorItem> EXTRACTOR =
            ITEMS.register("extractor", () -> new ExtractorItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<BlockItem> RESEARCH_STATION_ITEM =
            ITEMS.register("research_station", () -> new BlockItem(RESEARCH_STATION.get(), new Item.Properties()));

    public static final Supplier<MenuType<ResearchStationMenu>> RESEARCH_STATION_MENU =
            MENU_TYPES.register("research_station", () ->
                    new MenuType<>(ResearchStationMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MorphPlatformBlock> MORPH_PLATFORM =
            BLOCKS.register("morph_platform", () -> new MorphPlatformBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            ));

    public static final Supplier<BlockItem> MORPH_PLATFORM_ITEM =
            ITEMS.register("morph_platform", () -> new BlockItem(MORPH_PLATFORM.get(), new Item.Properties()));

    public static final Supplier<MenuType<MorphPlatformMenu>> MORPH_PLATFORM_MENU =
            MENU_TYPES.register("morph_platform", () ->
                    new MenuType<>(MorphPlatformMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<CreativeModeTab> ATOM_TAB =
            CREATIVE_TABS.register("atom", () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.atom"))
                    .icon(() -> new net.minecraft.world.item.ItemStack(EXTRACTOR.get()))
                    .displayItems((params, output) -> {
                        output.accept(EXTRACTOR.get());
                        output.accept(RESEARCH_STATION_ITEM.get());
                        output.accept(MORPH_PLATFORM_ITEM.get());
                    })
                    .build());

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    public static final Supplier<EntityType<ShellEntity>> SHELL_ENTITY =
            ENTITY_TYPES.register("shell", () ->
                    EntityType.Builder.<ShellEntity>of(ShellEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.8f)
                            .build("atom:shell"));

    public Atom(IEventBus modEventBus, ModContainer modContainer) {
        MorphAttachments.register(modEventBus);
        MorphUnlockAttachments.register(modEventBus);
        ShellAttachments.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class NetworkEvents {
        @SubscribeEvent
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar("1");
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.jsaperr.atom.client.ClientPayloadHandler.register(registrar);
            } else {
                registrar.playToClient(MorphSyncPayload.TYPE, MorphSyncPayload.STREAM_CODEC, (p, c) -> {});
                registrar.playToClient(ResearchSyncPayload.TYPE, ResearchSyncPayload.STREAM_CODEC, (p, c) -> {});
                registrar.playToClient(MorphSelectPayload.TYPE, MorphSelectPayload.STREAM_CODEC, (p, c) -> {});
            }
            registrar.playToServer(MorphRequestPayload.TYPE, MorphRequestPayload.STREAM_CODEC,
                    MorphEventHandler::handleMorphRequest);
            registrar.playToServer(ChickenFlutterPayload.TYPE, ChickenFlutterPayload.STREAM_CODEC,
                    MorphPassiveHandler::handleFlutter);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Atom.SHELL_ENTITY.get(),
                    com.jsaperr.atom.client.ShellEntityRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(Atom.RESEARCH_STATION_MENU.get(), ResearchStationScreen::new);
            event.register(Atom.MORPH_PLATFORM_MENU.get(), MorphPlatformScreen::new);
        }
    }
}
