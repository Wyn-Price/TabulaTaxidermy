package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class C5RequestHeaders {
    public static C5RequestHeaders fromBytes(PacketBuffer buf) {
        return new C5RequestHeaders();
    }

    public static void toBytes(C5RequestHeaders packet, PacketBuffer buf) {
    }

    public static void handle(C5RequestHeaders packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            for (DataHandler<?> handler : DataHandler.HANDLERS) {
                Taxidermy.NETWORK.send(PacketDistributor.PLAYER.with(() -> sender), new S6SendHeaders(handler, handler.getHeaders()));
            }
        });
        context.setPacketHandled(true);
    }
}
