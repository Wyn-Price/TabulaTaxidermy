package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.DataHandler;
import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C5RequestHeaders implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler extends WorldModificationsMessageHandler<C5RequestHeaders, S6SendHeaders> {
        @Override
        protected void handleMessage(C5RequestHeaders message, MessageContext ctx, World world, EntityPlayer player) {
            for (DataHandler<?> handler : DataHandler.HANDLERS) {
                TabulaTaxidermy.NETWORK.sendTo(new S6SendHeaders(handler, handler.getHeaders(world)), (EntityPlayerMP) player);
            }
        }
    }
}
