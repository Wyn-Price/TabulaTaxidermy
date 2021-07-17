package com.wynprice.taxidermy;

import com.wynprice.taxidermy.network.*;
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
    public static final RegistryObject<Block> BLOCK = B.register(MODID, () -> new TaxidermyBlock(AbstractBlock.Properties.of(Material.STONE).noCollission()));

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
        SplitNetworkHandler.registerMessage(C2SUploadData.class, C2SUploadData::toBytes, C2SUploadData::fromBytes, C2SUploadData::handle);
        NETWORK.registerMessage(1, C2SRequestDataForUUID.class, C2SRequestDataForUUID::toBytes, C2SRequestDataForUUID::fromBytes, C2SRequestDataForUUID::handle);
        SplitNetworkHandler.registerMessage(S2CSendDataToClient.class, S2CSendDataToClient::toBytes, S2CSendDataToClient::fromBytes, S2CSendDataToClient::handle);
        NETWORK.registerMessage(3, C2SSetBlockProperties.class, C2SSetBlockProperties::toBytes, C2SSetBlockProperties::fromBytes, C2SSetBlockProperties::handle);
        NETWORK.registerMessage(4, S2CSyncBlockProperties.class, S2CSyncBlockProperties::toBytes, S2CSyncBlockProperties::fromBytes, S2CSyncBlockProperties::handle);
        NETWORK.registerMessage(5, C2SRequestHeaders.class, C2SRequestHeaders::toBytes, C2SRequestHeaders::fromBytes, C2SRequestHeaders::handle);
        NETWORK.registerMessage(6, S2CSendHeaders.class, S2CSendHeaders::toBytes, S2CSendHeaders::fromBytes, S2CSendHeaders::handle);
        NETWORK.registerMessage(7, C2SSetBlockUUID.class, C2SSetBlockUUID::toBytes, C2SSetBlockUUID::fromBytes, C2SSetBlockUUID::handle);
        NETWORK.registerMessage(8, S2CSetBlockUUID.class, S2CSetBlockUUID::toBytes, S2CSetBlockUUID::fromBytes, S2CSetBlockUUID::handle);
        NETWORK.registerMessage(9, C2SToggleHidden.class, C2SToggleHidden::toBytes, C2SToggleHidden::fromBytes, C2SToggleHidden::handle);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Taxidermy::registerTESR);
    }

    private static void registerTESR() {
        ClientRegistry.bindTileEntityRenderer(BLOCK_ENTITY.get(), TaxidermyBlockEntityRenderer::new);
    }

    public static Logger getLogger() {
        return logger;
    }
}
