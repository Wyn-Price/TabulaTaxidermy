package com.wynprice.tabulataxidermy;

import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.dumblibrary.client.gui.GuiConstants;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Type;
import java.util.UUID;

@Value
public class DataHeader {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(DataHeader.class, IoHandler.INSTANCE).create();

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

    private enum IoHandler implements JsonSerializer<DataHeader>, JsonDeserializer<DataHeader> {
        INSTANCE;

        @Override
        public DataHeader deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
            return new DataHeader(
                UUID.fromString(JsonUtils.getString(object, "uuid")),
                JsonUtils.getString(object, "name"),
                JsonUtils.getString(object, "uploader")
            );
        }

        @Override
        public JsonElement serialize(DataHeader src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("uuid", src.getUuid().toString());
            json.addProperty("name", src.getName());
            json.addProperty("uploader", src.getUploader());

            return json;
        }
    }

}
