package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

@AllArgsConstructor
public class C1RequestDataForUUID {

    private UUID uuid;
    private DataHandler<?> dataHandler;

    public static C1RequestDataForUUID fromBytes(PacketBuffer buf) {
        return new C1RequestDataForUUID(buf.readUUID(), DataHandler.read(buf));
    }

    public static void toBytes(C1RequestDataForUUID packet, PacketBuffer buf) {
        buf.writeUUID(packet.uuid);
        DataHandler.write(buf, packet.dataHandler);
    }

    public static void handle(C1RequestDataForUUID message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = context.getSender().level;
            message.dataHandler.createHandler(message.uuid).ifPresent(h -> {
                SplitNetworkHandler.sendSplitMessage(new S2SendDataToClient(message.uuid, h), PacketDistributor.DIMENSION.with(world::dimension));
            });
        });
        context.setPacketHandled(true);
    }

}
