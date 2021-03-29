package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
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
public class C0UploadData {

    private BlockPos pos;
    private UUID uuid;
    private String name;
    private DataHandler.Handler<?> handler;

    public static C0UploadData fromBytes(PacketBuffer buf) {
        return new C0UploadData(buf.readBlockPos(), buf.readUUID(), buf.readUtf(), DataHandler.readHandler(buf));
    }

    public static void toBytes(C0UploadData packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUUID(packet.uuid);
        buf.writeUtf(packet.name);
        DataHandler.writeHandler(buf, packet.handler);
    }

    public static void handle(C0UploadData message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            TileEntity entity = world.getBlockEntity(message.pos);
            if(entity instanceof TaxidermyBlockEntity) {
                message.handler.applyTo((TaxidermyBlockEntity) entity, message.uuid);
                entity.setChanged();
                ((TaxidermyBlockEntity) entity).syncToClient();
            }
            message.handler.saveToFile(message.uuid, message.name, sender.getScoreboardName());

            DataHandler<?> handler = message.handler.getParent();
            Taxidermy.NETWORK.send(PacketDistributor.PLAYER.with(() -> sender), new S6SendHeaders(handler, handler.getHeaders()));
        });
        context.setPacketHandled(true);
    }
}
