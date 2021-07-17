package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

@AllArgsConstructor
public class C2SUploadData {

    private UUID uuid;
    private String name;
    private DataHandler.Handler<?> handler;

    public static C2SUploadData fromBytes(PacketBuffer buf) {
        return new C2SUploadData(buf.readUUID(), buf.readUtf(32767), DataHandler.readHandler(buf));
    }

    public static void toBytes(C2SUploadData packet, PacketBuffer buf) {
        buf.writeUUID(packet.uuid);
        buf.writeUtf(packet.name);
        DataHandler.writeHandler(buf, packet.handler);
    }

    public static void handle(C2SUploadData message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity entity = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                message.handler.applyTo((TaxidermyBlockEntity) entity, message.uuid);
                entity.setChanged();
                entity.syncToClient();
            }
            message.handler.saveToFile(message.uuid, message.name, sender.getScoreboardName());

            DataHandler<?> handler = message.handler.getParent();
            Taxidermy.NETWORK.send(PacketDistributor.PLAYER.with(() -> sender), new S2CSendHeaders(handler, handler.getHeaders()));
        });
        context.setPacketHandled(true);
    }
}
