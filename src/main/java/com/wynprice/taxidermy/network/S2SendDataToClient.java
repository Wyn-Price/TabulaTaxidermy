package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.TaxidermyClientCache;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class S2SendDataToClient implements IMessage {

    private UUID uuid;
    private DataHandler.Handler<?> handler;

    public S2SendDataToClient() {
    }

    public S2SendDataToClient(UUID uuid, DataHandler.Handler<?> handler) {
        this.uuid = uuid;
        this.handler = handler;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.handler = DataHandler.readHandler(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        DataHandler.writeHandler(buf, this.handler);
    }

    public static class Handler extends WorldModificationsMessageHandler<S2SendDataToClient, S2SendDataToClient> {

        @Override
        protected void handleMessage(S2SendDataToClient message, MessageContext ctx, World world, EntityPlayer player) {
            TaxidermyClientCache.handle(message.uuid, message.handler);
        }
    }
}
