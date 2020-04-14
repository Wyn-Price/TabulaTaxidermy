package com.wynprice.tabulataxidermy.network;

import com.google.gson.stream.JsonWriter;
import com.wynprice.tabulataxidermy.DataHandler;
import com.wynprice.tabulataxidermy.DataHeader;
import com.wynprice.tabulataxidermy.TTBlockEntity;
import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaBufferHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class C0UploadData implements IMessage {

    private BlockPos pos;
    private UUID uuid;
    private String name;
    private DataHandler.Handler<?> handler;

    public C0UploadData() {
    }

    public C0UploadData(BlockPos pos, UUID uuid, String name, DataHandler.Handler<?> handler) {
        this.pos = pos;
        this.uuid = uuid;
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.name = ByteBufUtils.readUTF8String(buf);
        this.handler = DataHandler.readHandler(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, this.name);
        DataHandler.writeHandler(buf, this.handler);
    }

    public static class Handler extends WorldModificationsMessageHandler<C0UploadData, C0UploadData> {

        @Override
        protected void handleMessage(C0UploadData message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.pos);
            if(entity instanceof TTBlockEntity) {
                message.handler.applyTo((TTBlockEntity) entity, message.uuid);
                entity.markDirty();
                ((TTBlockEntity) entity).syncToClient();
            }
            message.handler.saveToFile(world, message.uuid, message.name, player.getName());

            DataHandler<?> handler = message.handler.getParent();
            TabulaTaxidermy.NETWORK.sendTo(new S6SendHeaders(handler, handler.getHeaders(world)), (EntityPlayerMP) player);
        }
    }
}
