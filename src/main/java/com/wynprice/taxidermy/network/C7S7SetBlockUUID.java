package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import lombok.AllArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

@AllArgsConstructor
public class C7S7SetBlockUUID {

    private BlockPos pos;
    private UUID uuid;
    private DataHandler<?> handler;

    public static C7S7SetBlockUUID fromBytes(PacketBuffer buf) {
        return new C7S7SetBlockUUID(
            buf.readBlockPos(),
            buf.readUUID(),
            DataHandler.read(buf)
        );
    }

    public static void toBytes(C7S7SetBlockUUID packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUUID(packet.uuid);
        DataHandler.write(buf, packet.handler);
    }

    public static void handle(C7S7SetBlockUUID message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = context.getSender().level;
            TileEntity entity = world.getBlockEntity(message.pos);
            if(entity instanceof TaxidermyBlockEntity) {
                message.handler.applyTo((TaxidermyBlockEntity) entity, message.uuid);
            }
            if(!world.isClientSide) {
                Taxidermy.NETWORK.send(PacketDistributor.DIMENSION.with(world::dimension), new C7S7SetBlockUUID(message.pos, message.uuid, message.handler));
            }
        });
    }
}
