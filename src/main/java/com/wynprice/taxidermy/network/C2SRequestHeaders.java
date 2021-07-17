package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SRequestHeaders {
    public static C2SRequestHeaders fromBytes(PacketBuffer buf) {
        return new C2SRequestHeaders();
    }

    public static void toBytes(C2SRequestHeaders packet, PacketBuffer buf) {
    }

    public static void handle(C2SRequestHeaders packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            for (DataHandler<?> handler : DataHandler.HANDLERS) {
                Taxidermy.NETWORK.send(PacketDistributor.PLAYER.with(() -> sender), new S2CSendHeaders(handler, handler.getHeaders()));
            }
        });
        context.setPacketHandled(true);
    }
}
