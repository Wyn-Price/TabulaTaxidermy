package com.wynprice.taxidermy;

import com.wynprice.taxidermy.network.*;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Taxidermy.MODID)
@Mod.EventBusSubscriber
public class Taxidermy {
    public static final String MODID = "taxidermy";
    public static final String NAME = "Taxidermy";
    public static final String VERSION = "0.2.2";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static final ItemGroup TAB = new ItemGroup(MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ITEM.get());
        }
    };

    private static final DeferredRegister<Block> B = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> BLOCK = B.register(MODID, () -> new TaxidermyBlock(AbstractBlock.Properties.of(Material.STONE)));

    private static final DeferredRegister<Item> I = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> ITEM = I.register(MODID, () -> new BlockItem(BLOCK.get(), new Item.Properties().tab(TAB)));

    private static final DeferredRegister<TileEntityType<?>> T = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final RegistryObject<TileEntityType<TaxidermyBlockEntity>> BLOCK_ENTITY = T.register(MODID, () -> TileEntityType.Builder.of(TaxidermyBlockEntity::new, BLOCK.get()).build(null));


    private static Logger logger = LogManager.getLogger(MODID);

    public Taxidermy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(this::preInit);

        B.register(bus);
        I.register(bus);
        T.register(bus);
    }

    public void preInit(FMLCommonSetupEvent event) {

        SplitNetworkHandler.registerMessage(C0UploadData.class, C0UploadData::toBytes, C0UploadData::fromBytes, C0UploadData::handle);
        NETWORK.registerMessage(1, C1RequestDataForUUID.class, C1RequestDataForUUID::toBytes, C1RequestDataForUUID::fromBytes, C1RequestDataForUUID::handle);
        SplitNetworkHandler.registerMessage(S2SendDataToClient.class, S2SendDataToClient::toBytes, S2SendDataToClient::fromBytes, S2SendDataToClient::handle);
        NETWORK.registerMessage(3, C3SetBlockProperties.class, C3SetBlockProperties::toBytes, C3SetBlockProperties::fromBytes, C3SetBlockProperties::handle);
        NETWORK.registerMessage(4, S4SyncBlockProperties.class, S4SyncBlockProperties::toBytes, S4SyncBlockProperties::fromBytes, S4SyncBlockProperties::handle);
        NETWORK.registerMessage(5, C5RequestHeaders.class, C5RequestHeaders::toBytes, C5RequestHeaders::fromBytes, C5RequestHeaders::handle);
        NETWORK.registerMessage(6, S6SendHeaders.class, S6SendHeaders::toBytes, S6SendHeaders::fromBytes, S6SendHeaders::handle);
        NETWORK.registerMessage(7, C7S7SetBlockUUID.class, C7S7SetBlockUUID::toBytes, C7S7SetBlockUUID::fromBytes, C7S7SetBlockUUID::handle);
        NETWORK.registerMessage(8, C8ToggleHidden.class, C8ToggleHidden::toBytes, C8ToggleHidden::fromBytes, C8ToggleHidden::handle);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Taxidermy::registerTESR);
    }

    private static void registerTESR() {
        ClientRegistry.bindTileEntityRenderer(BLOCK_ENTITY.get(), TaxidermyBlockEntityRenderer::new);
    }

    public static Logger getLogger() {
        return logger;
    }
}
