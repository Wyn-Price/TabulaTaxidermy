package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
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

import java.util.UUID;
import java.util.function.Supplier;

@AllArgsConstructor
public class C2SSetBlockUUID {

    private UUID uuid;
    private DataHandler<?> handler;

    public static C2SSetBlockUUID fromBytes(PacketBuffer buf) {
        return new C2SSetBlockUUID(
            buf.readUUID(),
            DataHandler.read(buf)
        );
    }

    public static void toBytes(C2SSetBlockUUID packet, PacketBuffer buf) {
        buf.writeUUID(packet.uuid);
        DataHandler.write(buf, packet.handler);
    }

    public static void handle(C2SSetBlockUUID message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity blockEntity = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                message.handler.applyTo((TaxidermyBlockEntity) blockEntity, message.uuid);
                Taxidermy.NETWORK.send(PacketDistributor.DIMENSION.with(world::dimension), new S2CSetBlockUUID(blockEntity.getBlockPos(), message.uuid, message.handler));
            }

        });
        context.setPacketHandled(true);
    }
}
