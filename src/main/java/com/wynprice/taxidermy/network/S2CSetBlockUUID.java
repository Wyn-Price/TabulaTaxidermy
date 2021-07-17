package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
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
public class S2CSetBlockUUID {

    private BlockPos pos;
    private UUID uuid;
    private DataHandler<?> handler;

    public static S2CSetBlockUUID fromBytes(PacketBuffer buf) {
        return new S2CSetBlockUUID(
            buf.readBlockPos(),
            buf.readUUID(),
            DataHandler.read(buf)
        );
    }

    public static void toBytes(S2CSetBlockUUID packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUUID(packet.uuid);
        DataHandler.write(buf, packet.handler);
    }

    public static void handle(S2CSetBlockUUID message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientWorld level = Minecraft.getInstance().level;
            TileEntity entity = level.getBlockEntity(message.pos);
            if(entity instanceof TaxidermyBlockEntity) {
                message.handler.applyTo((TaxidermyBlockEntity) entity, message.uuid);

            }
        });
        context.setPacketHandled(true);
    }
}
