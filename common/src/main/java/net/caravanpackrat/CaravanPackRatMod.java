package net.caravanpackrat;

// based on: https://github.com/Tutorials-By-Kaupenjoe/Fabric-Tutorial-1.20.X/tree/30-blockEntity

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.util.InputUtil;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

public class CaravanPackRatMod {
    public static final String MOD_ID = "caravanpackrat";

    public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLERS = DeferredRegister.create(MOD_ID, RegistryKeys.SCREEN_HANDLER);

    static final RegistrySupplier<ScreenHandlerType<CaravanDepositScreenHandler>> CARAVAN_SCREEN_HANDLER_TYPE = SCREEN_HANDLERS.register(new Identifier(MOD_ID,"caravan_deposit_menu"), () -> new ScreenHandlerType<>(CaravanDepositScreenHandler::new, FeatureSet.empty()));

    // Registering a new creative tab
    /*
    public static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM_GROUP);
    public static final RegistrySupplier<ItemGroup> CARAVAN_DEPOT_ITEM_GROUP = ITEM_GROUPS.register("caravan_depot", () ->
            CreativeTabRegistry.create(Text.translatable("itemGroup." + MOD_ID + ".creative_tab"),
     */

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final RegistrySupplier<Block> CARAVAN_DEPOT_BLOCK = BLOCKS.register("caravan_depot", () ->
            new CaravanDepotBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_SLAB)));

    public static final RegistrySupplier<BlockEntityType<?>> CARAVAN_DEPOT_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("caravan_depot", () ->
            BlockEntityType.Builder.create(CaravanDepotBlockEntity::new).build(null));
    public static final RegistrySupplier<Item> CARAVAN_DEPOT_ITEM = ITEMS.register("caravan_depot", () ->
            new BlockItem(CARAVAN_DEPOT_BLOCK.get(), new BlockItem.Settings().arch$tab(ItemGroups.INVENTORY/*CaravanPackRatMod.CARAVAN_DEPOT_ITEM_GROUP*/)));

    public static final Identifier OPEN_CARAVAN_DEPOSIT_SCREEN_HANDLER_PACKET = new Identifier("caravanpackrat", "open_caravan_deposit_menu");

    // A key mapping with keyboard as the default
    public static final KeyBinding CUSTOM_KEYMAPPING = new KeyBinding(
            // TODO: find a way to make this configurable
            "key.caravanpackrat.open_caravan_deposit_menu", // The translation key of the name shown in the Controls screen
            InputUtil.Type.KEYSYM, // This key mapping is for Keyboards by default
            InputUtil.GLFW_KEY_P, // The default keycode
            "category.caravanpackrat.caravan_menus" // The category translation key used to categorize in the Controls screen
    );

    public static void init() {
        SCREEN_HANDLERS.register();
        //ITEM_GROUPS.register();
        BLOCK_ENTITY_TYPES.register();
        BLOCKS.register();
        ITEMS.register();

        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> MenuRegistry.registerScreenFactory(CARAVAN_SCREEN_HANDLER_TYPE.get(), CaravanDepositHandledScreen::new));

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (CUSTOM_KEYMAPPING.wasPressed()) {
                assert minecraft.player != null;
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                NetworkManager.sendToServer(OPEN_CARAVAN_DEPOSIT_SCREEN_HANDLER_PACKET, buf);
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, OPEN_CARAVAN_DEPOSIT_SCREEN_HANDLER_PACKET, (buf, context) -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            PlayerState playerState = PlayerStateManager.getPlayerState(player);
            if (playerState.depot != null) {
                MenuRegistry.openMenu(player, new NamedScreenHandlerFactory() {
                    @Nullable
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return CARAVAN_SCREEN_HANDLER_TYPE.get().create(syncId, playerInventory);
                    }

                    @Override
                    public @NotNull Text getDisplayName() {
                        return Text.translatable("container.caravanpackrat.caravan_deposit_menu");
                    }
                });
            }
        });

        //System.out.println(ModExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
