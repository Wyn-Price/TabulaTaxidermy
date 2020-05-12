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
    private Vector3f position;
    private Vector3f rotation;
    private float scale;

    public S4SyncBlockProperties() {
    }

    public S4SyncBlockProperties(BlockPos blockPos, Vector3f position, Vector3f rotation, float scale) {
        this.blockPos = blockPos;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockPos = BlockPos.fromLong(buf.readLong());
        this.position = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        this.rotation = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        this.scale = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.blockPos.toLong());

        buf.writeFloat(this.position.x);
        buf.writeFloat(this.position.y);
        buf.writeFloat(this.position.z);

        buf.writeFloat(this.rotation.x);
        buf.writeFloat(this.rotation.y);
        buf.writeFloat(this.rotation.z);

        buf.writeFloat(this.scale);
    }

    public static class Handler extends WorldModificationsMessageHandler<S4SyncBlockProperties, S4SyncBlockProperties>  {

        @Override
        protected void handleMessage(S4SyncBlockProperties message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.blockPos);
            if(entity instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity) entity).setTranslation(message.position);
                ((TaxidermyBlockEntity) entity).setRotation(message.rotation);
                ((TaxidermyBlockEntity) entity).setScale(message.scale);
            }
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof GuiTaxidermyBlock && ((GuiTaxidermyBlock) screen).getBlockEntity().getPos().equals(message.blockPos)) {
                ((GuiTaxidermyBlock) screen).setProperties(message.position, message.rotation, message.scale);
            }
        }
    }
}
