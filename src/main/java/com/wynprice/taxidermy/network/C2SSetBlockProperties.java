package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SSetBlockProperties {

    private int index;
    private float value;

    public static C2SSetBlockProperties fromBytes(PacketBuffer buf) {
        return new C2SSetBlockProperties(
            buf.readByte(), buf.readFloat()
        );
    }

    public static void toBytes(C2SSetBlockProperties packet, PacketBuffer buf) {
        buf.writeByte(packet.index);
        buf.writeFloat(packet.value);
    }

    public static void handle(C2SSetBlockProperties message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity entity = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                ((TaxidermyBlockEntity) entity).setProperty(message.index, message.value);
                entity.setChanged();
                Taxidermy.NETWORK.send(PacketDistributor.DIMENSION.with(world::dimension), new S2CSyncBlockProperties(entity.getBlockPos(), message.index, message.value));
            }
        });
        context.setPacketHandled(true);
    }


}
