package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.TTClientCache;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaBufferHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.ImageBufHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class S2SendDataToClient implements IMessage {

    private UUID uuid;
    private TabulaModelInformation modelInfo;
    private BufferedImage image;

    public S2SendDataToClient() {
    }

    public S2SendDataToClient(UUID uuid, TabulaModelInformation modelInfo, BufferedImage image) {
        this.uuid = uuid;
        this.image = image;
        this.modelInfo = modelInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.modelInfo = TabulaBufferHandler.INSTANCE.deserialize(buf);
        this.image = ImageBufHandler.INSTANCE.deserialize(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        TabulaBufferHandler.INSTANCE.serialize(buf, this.modelInfo);
        ImageBufHandler.INSTANCE.serialize(buf, this.image);
    }

    public static class Handler extends WorldModificationsMessageHandler<S2SendDataToClient, S2SendDataToClient> {

        @Override
        protected void handleMessage(S2SendDataToClient message, MessageContext ctx, World world, EntityPlayer player) {
            TTClientCache.INSTANCE.put(message.uuid, message.modelInfo, message.image);
        }
    }
}
