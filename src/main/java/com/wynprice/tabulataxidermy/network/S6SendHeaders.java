package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.DataHeader;
import com.wynprice.tabulataxidermy.GuiTTBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class S6SendHeaders implements IMessage {

    private List<DataHeader> headers;

    public S6SendHeaders() {
    }

    public S6SendHeaders(List<DataHeader> headers) {
        this.headers = headers;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.headers = new ArrayList<>();
        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.headers.add(DataHeader.readFromBuf(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.headers.size());
        for (DataHeader header : this.headers) {
            DataHeader.writeToBuf(header, buf);
        }
    }

    public static class Handler implements IMessageHandler<S6SendHeaders, IMessage> {

        @Override
        public IMessage onMessage(S6SendHeaders message, MessageContext ctx) {
            if(Minecraft.getMinecraft().currentScreen instanceof GuiTTBlock) {
                ((GuiTTBlock) Minecraft.getMinecraft().currentScreen).setList(message.headers);
            }
            return null;
        }
    }
}
