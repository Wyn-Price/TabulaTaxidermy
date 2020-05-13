package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.GuiTaxidermyBlock;
import com.wynprice.taxidermy.TaxidermyBlockEntity;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;

public class S4SyncBlockProperties implements IMessage {

    private BlockPos blockPos;
    private int index;
    private float value;

    public S4SyncBlockProperties() {
    }

    public S4SyncBlockProperties(BlockPos blockPos, int index, float value) {
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


    public static class Handler extends WorldModificationsMessageHandler<S4SyncBlockProperties, S4SyncBlockProperties>  {

        @Override
        protected void handleMessage(S4SyncBlockProperties message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.blockPos);
            if(entity instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity) entity).setProperty(message.index, message.value);
            }
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof GuiTaxidermyBlock && ((GuiTaxidermyBlock) screen).getBlockEntity().getPos().equals(message.blockPos)) {
                ((GuiTaxidermyBlock) screen).setProperties(message.index, message.value);
            }
        }
    }
}
