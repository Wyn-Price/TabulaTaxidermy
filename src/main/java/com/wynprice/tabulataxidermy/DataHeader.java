package com.wynprice.tabulataxidermy;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

@Value
public class DataHeader {
    private final UUID uuid;
    private final String name;
    private final String uploader;

    public static void writeToBuf(DataHeader header, ByteBuf buf) {
        buf.writeLong(header.getUuid().getMostSignificantBits());
        buf.writeLong(header.getUuid().getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, header.getName());
        ByteBufUtils.writeUTF8String(buf, header.getUploader());
    }

    public static DataHeader readFromBuf(ByteBuf buf) {
        return new DataHeader(
            new UUID(buf.readLong(), buf.readLong()),
            ByteBufUtils.readUTF8String(buf),
            ByteBufUtils.readUTF8String(buf)
        );
    }
}
