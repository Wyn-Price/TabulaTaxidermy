package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.TabulaTaxidermy;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C3SetBlockProperties implements IMessage {

    private BlockPos blockPos;
    private int index;
    private float value;

    public C3SetBlockProperties() {
    }

    public C3SetBlockProperties(BlockPos blockPos, int index, float value) {
        this.blockPos = blockPos;
        this.index = index;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockPos = BlockPos.fromLong(buf.readLong());
        this.index = buf.readByte();
        this.value = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.blockPos.toLong());
        buf.writeByte(this.index);
        buf.writeFloat(this.value);
    }

    public static class Handler extends WorldModificationsMessageHandler<C3SetBlockProperties, C3SetBlockProperties>  {

        @Override
        protected void handleMessage(C3SetBlockProperties message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.blockPos);
            if(entity instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity) entity).setProperty(message.index, message.value);
                entity.markDirty();
            }
            TabulaTaxidermy.NETWORK.sendToDimension(new S4SyncBlockProperties(message.blockPos, message.index, message.value), world.provider.getDimension());
        }
    }
}
