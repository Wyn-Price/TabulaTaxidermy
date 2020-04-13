package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.DataHeader;
import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class C5RequestHeaders implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler extends WorldModificationsMessageHandler<C5RequestHeaders, S6SendHeaders>
    {
        @Override
        protected void handleMessage(C5RequestHeaders message, MessageContext ctx, World world, EntityPlayer player) {
            //NO-OP
        }

        @Override
        protected S6SendHeaders answer(C5RequestHeaders message, MessageContext ctx, World world, EntityPlayer player) {
            File storage = new File(world.getSaveHandler().getWorldDirectory(), "taxidermy_storage");
            if(!storage.exists()) {
                return null;
            }
            List<DataHeader> headerList = new ArrayList<>();
            for (File file : Objects.requireNonNull(storage.listFiles(), "Storage List: " + storage)) {
                try {
                    headerList.add(DataHeader.GSON.fromJson(new FileReader(new File(file, "data.json")), DataHeader.class));
                } catch (IOException e) {
                    TabulaTaxidermy.getLogger().error("Unable to load data.json", e);
                }
            }
            return new S6SendHeaders(headerList);
        }
    }
}
