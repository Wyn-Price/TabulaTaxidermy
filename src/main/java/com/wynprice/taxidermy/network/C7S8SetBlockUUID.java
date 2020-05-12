package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import com.wynprice.taxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class C7S8SetBlockUUID implements IMessage {

    private BlockPos pos;
    private UUID uuid;
    private DataHandler handler;

    public C7S8SetBlockUUID() {
    }

    public C7S8SetBlockUUID(BlockPos pos, UUID uuid, DataHandler handler) {
        this.pos = pos;
        this.uuid = uuid;
        this.handler = handler;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.handler = DataHandler.read(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        DataHandler.write(buf, this.handler);
    }

    public static class Handler extends WorldModificationsMessageHandler<C7S8SetBlockUUID, IMessage> {

        @Override
        protected void handleMessage(C7S8SetBlockUUID message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.pos);
            if(entity instanceof TaxidermyBlockEntity) {
                message.handler.applyTo((TaxidermyBlockEntity) entity, message.uuid);
            }
            if(!world.isRemote) {
                TabulaTaxidermy.NETWORK.sendToDimension(new C7S8SetBlockUUID(message.pos, message.uuid, message.handler), world.provider.getDimension());
            }
        }
    }
}
