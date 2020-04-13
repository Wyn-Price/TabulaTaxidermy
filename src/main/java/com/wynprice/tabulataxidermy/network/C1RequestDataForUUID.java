package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class C1RequestDataForUUID implements IMessage {

    private UUID uuid;

    public C1RequestDataForUUID() {
    }

    public C1RequestDataForUUID(UUID uuid) {
        this.uuid = uuid;

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
    }

    public static class Handler extends WorldModificationsMessageHandler<C1RequestDataForUUID, C1RequestDataForUUID> {

        @Override
        protected void handleMessage(C1RequestDataForUUID message, MessageContext ctx, World world, EntityPlayer player) {
            File storage = new File(world.getSaveHandler().getWorldDirectory(), "taxidermy_storage/" + message.uuid.toString().replaceAll("-", ""));

            TabulaModelInformation information = null;
            try {
                File model = new File(storage, "tabula_model.tbl");
                information = TabulaUtils.getModelInformation(new FileInputStream(model));
            } catch (IOException e) {
                TabulaTaxidermy.getLogger().error("Unable to load model file", e);
            }

            BufferedImage image = null;
            try {
                File texture = new File(storage, "texture.png");
                image = ImageIO.read(texture);
            } catch (IOException e) {
                TabulaTaxidermy.getLogger().error("Unable to save texture file", e);
            }

            if(information != null && image != null) {
                SplitNetworkHandler.sendSplitMessage(new S2SendDataToClient(message.uuid, information, image), (wrapper, m) -> wrapper.sendToDimension(m, world.provider.getDimension()));
            }
        }
    }
}
