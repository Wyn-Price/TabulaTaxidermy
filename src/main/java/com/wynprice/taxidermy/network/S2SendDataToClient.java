package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.TaxidermyClientCache;
import lombok.AllArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

@AllArgsConstructor
public class S2SendDataToClient {

    private UUID uuid;
    private DataHandler.Handler<?> handler;

    public static S2SendDataToClient fromBytes(PacketBuffer buf) {
        return new S2SendDataToClient(
            buf.readUUID(),
            DataHandler.readHandler(buf)
        );
    }

    public static void toBytes(S2SendDataToClient packet, PacketBuffer buf) {
        buf.writeUUID(packet.uuid);
        DataHandler.writeHandler(buf, packet.handler);
    }

    public static void handle(S2SendDataToClient message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> TaxidermyClientCache.handle(message.uuid, message.handler));
    }
}
