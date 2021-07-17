package com.wynprice.taxidermy;

import lombok.Value;
import net.minecraft.network.PacketBuffer;

import java.util.UUID;

@Value
public class DataHeader {
    private final UUID uuid;
    private final String name;
    private final String uploader;

    public static void writeToBuf(DataHeader header, PacketBuffer buf) {
        buf.writeUUID(header.getUuid());
        buf.writeUtf(header.getName());
        buf.writeUtf(header.getUploader());
    }

    public static DataHeader readFromBuf(PacketBuffer buf) {
        return new DataHeader(
            buf.readUUID(),
            buf.readUtf(32767),
            buf.readUtf(32767)
        );
    }
}
