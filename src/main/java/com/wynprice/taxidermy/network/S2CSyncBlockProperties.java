package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.GuiTaxidermyBlock;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S2CSyncBlockProperties {

    private BlockPos blockPos;
    private int index;
    private float value;

    public static S2CSyncBlockProperties fromBytes(PacketBuffer buf) {
        return new S2CSyncBlockProperties(
            buf.readBlockPos(), buf.readByte(), buf.readFloat()
        );
    }

    public static void toBytes(S2CSyncBlockProperties packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.blockPos);
        buf.writeByte(packet.index);
        buf.writeFloat(packet.value);
    }

    public static void handle(S2CSyncBlockProperties message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientWorld world = Minecraft.getInstance().level;
            TileEntity entity = world.getBlockEntity(message.blockPos);
            if(entity instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity) entity).setProperty(message.index, message.value);
                entity.setChanged();
            }
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof GuiTaxidermyBlock && ((GuiTaxidermyBlock) screen).getBlockEntity().getBlockPos().equals(message.blockPos)) {
                ((GuiTaxidermyBlock) screen).setProperties(message.index, message.value);
            }
        });
        context.setPacketHandled(true);
    }


}
