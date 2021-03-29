package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.DataHandler;
import com.wynprice.taxidermy.DataHeader;
import com.wynprice.taxidermy.GuiTaxidermyBlock;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
public class S6SendHeaders {

    private DataHandler<?> handler;
    private List<DataHeader> headers;

    public static S6SendHeaders fromBytes(PacketBuffer buf) {
        return new S6SendHeaders(
            DataHandler.read(buf),
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> DataHeader.readFromBuf(buf))
                .collect(Collectors.toList())
        );
    }

    public static void toBytes(S6SendHeaders packet, PacketBuffer buf) {
        DataHandler.write(buf, packet.handler);
        buf.writeShort(packet.headers.size());
        for (DataHeader header : packet.headers) {
            DataHeader.writeToBuf(header, buf);
        }
    }

    public static void handle(S6SendHeaders message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof GuiTaxidermyBlock) {
                ((GuiTaxidermyBlock) screen).setList(message.handler, message.headers);
            }
        });
        context.setPacketHandled(true);
    }
}
