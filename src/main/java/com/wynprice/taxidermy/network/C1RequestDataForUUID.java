package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class C1RequestDataForUUID implements IMessage {

    private UUID uuid;
    private DataHandler<?> dataHandler;

    public C1RequestDataForUUID() {
    }

    public C1RequestDataForUUID(UUID uuid, DataHandler<?> dataHandler) {
        this.uuid = uuid;
        this.dataHandler = dataHandler;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.dataHandler = DataHandler.read(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        DataHandler.write(buf, this.dataHandler);
    }

    public static class Handler extends WorldModificationsMessageHandler<C1RequestDataForUUID, C1RequestDataForUUID> {

        @Override
        protected void handleMessage(C1RequestDataForUUID message, MessageContext ctx, World world, EntityPlayer player) {
            message.dataHandler.createHandler(world, message.uuid).ifPresent(h ->
                SplitNetworkHandler.sendSplitMessage(new S2SendDataToClient(message.uuid, h), (wrapper, m) -> wrapper.sendToDimension(m, world.provider.getDimension()))
            );
        }
    }
}
