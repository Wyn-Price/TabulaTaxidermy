package com.wynprice.tabulataxidermy;

import com.wynprice.tabulataxidermy.network.*;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = TabulaTaxidermy.MODID, name = TabulaTaxidermy.NAME, version = TabulaTaxidermy.VERSION)
@Mod.EventBusSubscriber
public class TabulaTaxidermy {
    public static final String MODID = "tabulataxidermy";
    public static final String NAME = "Tabula Taxidermy";
    public static final String VERSION = "1.0.0";

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    @GameRegistry.ObjectHolder(TabulaTaxidermy.MODID + ":taxi_block")
    public static final Block BLOCK = InjectedUtils.injected();

    @GameRegistry.ObjectHolder(TabulaTaxidermy.MODID + ":taxi_block")
    public static final Item ITEM = InjectedUtils.injected();

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        SplitNetworkHandler.registerPacket(C0UploadData.class, new C0UploadData.Handler());
        NETWORK.registerMessage(new C1RequestDataForUUID.Handler(), C1RequestDataForUUID.class, 1, Side.SERVER);
        SplitNetworkHandler.registerPacket(S2SendDataToClient.class, new S2SendDataToClient.Handler());
        NETWORK.registerMessage(new C3SetBlockProperties.Handler(), C3SetBlockProperties.class, 3, Side.SERVER);
        NETWORK.registerMessage(new S4SyncBlockProperties.Handler(), S4SyncBlockProperties.class, 4, Side.CLIENT);
        NETWORK.registerMessage(new C5RequestHeaders.Handler(), C5RequestHeaders.class, 5, Side.SERVER);
        NETWORK.registerMessage(new S6SendHeaders.Handler(), S6SendHeaders.class, 6, Side.CLIENT);
        NETWORK.registerMessage(new C7SetBlockUUID.Handler(), C7SetBlockUUID.class, 7, Side.SERVER);
        NETWORK.registerMessage(new S8SyncBlockUUID.Handler(), S8SyncBlockUUID.class, 8, Side.CLIENT);
        NETWORK.registerMessage(new C9ToggleHidden.Handler(), C9ToggleHidden.class, 9, Side.SERVER);

        GameRegistry.registerTileEntity(TTBlockEntity.class, new ResourceLocation(MODID, "taxi_block"));

        ClientRegistry.bindTileEntitySpecialRenderer(TTBlockEntity.class, new TTBlockEntityRenderer());

    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new TTBlock(Material.IRON).setRegistryName("taxi_block").setTranslationKey(MODID + ".taxi_block"));
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(BLOCK).setRegistryName("taxi_item").setTranslationKey(MODID + ".taxi_block"));
    }

    public static Logger getLogger() {
        return logger;
    }
}
