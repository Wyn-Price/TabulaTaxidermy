package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C3SetBlockProperties {

    private BlockPos blockPos;
    private int index;
    private float value;

    public static C3SetBlockProperties fromBytes(PacketBuffer buf) {
        return new C3SetBlockProperties(
            buf.readBlockPos(), buf.readByte(), buf.readFloat()
        );
    }

    public static void toBytes(C3SetBlockProperties packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.blockPos);
        buf.writeByte(packet.index);
        buf.writeFloat(packet.value);
    }

    public static void handle(C3SetBlockProperties message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = context.getSender().level;
            TileEntity entity = world.getBlockEntity(message.blockPos);
            if(entity instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity) entity).setProperty(message.index, message.value);
                entity.setChanged();
            }
            Taxidermy.NETWORK.send(PacketDistributor.DIMENSION.with(world::dimension), new S4SyncBlockProperties(message.blockPos, message.index, message.value));
        });
        context.setPacketHandled(true);
    }


}
