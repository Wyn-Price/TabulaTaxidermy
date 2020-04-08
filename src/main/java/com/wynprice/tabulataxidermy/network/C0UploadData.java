package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.TTBlockEntity;
import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaBufferHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.ImageBufHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.core.util.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class C0UploadData implements IMessage {

    private BlockPos pos;
    private UUID uuid;
    private TabulaModelInformation modelInfo;
    private BufferedImage image;

    public C0UploadData() {
    }

    public C0UploadData(BlockPos pos, UUID uuid, TabulaModelInformation modelInfo, BufferedImage image) {
        this.pos = pos;
        this.uuid = uuid;
        this.image = image;
        this.modelInfo = modelInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.modelInfo = TabulaBufferHandler.INSTANCE.deserialize(buf);
        this.image = ImageBufHandler.INSTANCE.deserialize(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        TabulaBufferHandler.INSTANCE.serialize(buf, this.modelInfo);
        ImageBufHandler.INSTANCE.serialize(buf, this.image);
    }

    public static class Handler extends WorldModificationsMessageHandler<C0UploadData, C0UploadData> {

        @Override
        protected void handleMessage(C0UploadData message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.pos);
            if(entity instanceof TTBlockEntity) {
                ((TTBlockEntity) entity).setDataUUID(message.uuid);
                entity.markDirty();
                ((TTBlockEntity) entity).syncToClient();
            }
            File storage = world.getMinecraftServer().getFile("taxidermy_storage/" + message.uuid.toString().replaceAll("-", ""));
            if(!storage.exists()) {
                try {
                    FileUtils.mkdir(storage, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            try {
                File model = new File(storage, "tabula_model.tbl");
                TabulaUtils.writeToStream(message.modelInfo, new FileOutputStream(model));
            } catch (IOException e) {
                TabulaTaxidermy.getLogger().error("Unable to save model file", e);
            }
            try {
                File texture = new File(storage, "texture.png");
                ImageIO.write(message.image, "PNG", texture);
            } catch (IOException e) {
                TabulaTaxidermy.getLogger().error("Unable to save texture file", e);
            }

        }
    }
}
