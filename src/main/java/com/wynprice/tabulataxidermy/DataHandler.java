package com.wynprice.tabulataxidermy;

import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.tabula.TabulaBufferHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.ImageBufferHandler;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DataHandler<O> {
    public static final DataHandler<BufferedImage> TEXTURE = new DataHandler<>(
        "texture", ".png", TTBlockEntity::setTextureUUID, ImageBufferHandler.INSTANCE,
        (img, stream) -> ImageIO.write(img, "PNG", stream), ImageIO::read
    );

    public static final DataHandler<TabulaModelInformation> MODEL = new DataHandler<>(
        "model", ".tbl", TTBlockEntity::setModelUUID, TabulaBufferHandler.INSTANCE,
        TabulaUtils::writeToStream, TabulaUtils::getModelInformation
    );

    public static final DataHandler[] HANDLERS = { TEXTURE, MODEL };

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private final String typeName;
    @Getter
    private final String extension;
    private final BiConsumer<TTBlockEntity, UUID> uuidSetter;

    //IO Stuff:
    private final BiConsumer<ByteBuf, O> seralizer;
    private final Function<ByteBuf, O> deseralizer;
    private final ThrowableBiConsumer<O, OutputStream> filsSeralizer;
    private final ThrowableFunction<InputStream, O> fileDeseralizer;

    public <T extends BiConsumer<ByteBuf, O> & Function<ByteBuf, O>> DataHandler(
        String typeName, String extension, BiConsumer<TTBlockEntity, UUID> uuidSetter, T handler,
        ThrowableBiConsumer<O, OutputStream> filsSeralizer, ThrowableFunction<InputStream, O> fileDeseralizer
    ) {
        this.typeName = typeName;
        this.extension = extension;
        this.uuidSetter = uuidSetter;
        this.seralizer = handler;
        this.deseralizer = handler;
        this.filsSeralizer = filsSeralizer;
        this.fileDeseralizer = fileDeseralizer;
    }

    public File getBaseFolder(World world) {
        File file =  new File(world.getSaveHandler().getWorldDirectory(), "taxidermy_storage/" + this.typeName);
        if(!file.exists()) {
            try {
                FileUtils.forceMkdir(file);
            } catch (IOException e) {
                //Maybe don't throw exception?
                throw new IllegalArgumentException("Unable to create base folder at file '" + file.getAbsolutePath() + "'", e);
            }
        }
        return file;
    }

    private File getUUIDFile(World world, UUID uuid) {
        return new File(this.getBaseFolder(world), uuid.toString().replaceAll("-", "") + this.extension);
    }

    private File getJsonFile(World world) {
        return new File(this.getBaseFolder(world), "data.json");
    }

    public List<DataHeader> getHeaders(World world) {
        File file = this.getJsonFile(world);

        List<DataHeader> headers = new ArrayList<>();
        try {
            if(file.exists()) {
                JsonParser parser = new JsonParser();
                for (Map.Entry<String, JsonElement> entry : parser.parse(new FileReader(file)).getAsJsonObject().entrySet()) {
                    JsonObject json = entry.getValue().getAsJsonObject();
                    headers.add(new DataHeader(UUID.fromString(entry.getKey()), JsonUtils.getString(json, "name"), JsonUtils.getString(json, "uploader")));
                }
            }
        } catch (IOException e) {
            TabulaTaxidermy.getLogger().error("Unable to read data file at '" + file.getAbsolutePath() + "'", e);
        }
        return headers;
    }

    private void writeHeaders(World world, List<DataHeader> headers) {
        File file = this.getJsonFile(world);
        try {
            @Cleanup FileWriter writer = new FileWriter(file);

            JsonObject json = new JsonObject();
            for (DataHeader header : headers) {
                JsonObject obj = new JsonObject();
                json.add(header.getUuid().toString(), obj);

                obj.addProperty("name", header.getName());
                obj.addProperty("uploader", header.getUploader());
            }

            writer.write(GSON.toJson(json));
        } catch (IOException e) {
            TabulaTaxidermy.getLogger().error("Unable to write data file at '" + file.getAbsolutePath() + "'", e);
        }
    }

    public void appendJsonFile(World world, UUID uuid, String name, String uploader) {
        List<DataHeader> headers = this.getHeaders(world);
        headers.add(new DataHeader(uuid, name, uploader));
        this.writeHeaders(world, headers);
    }

    public void applyTo(TTBlockEntity blockEntity, UUID uuid) {
        this.uuidSetter.accept(blockEntity, uuid);
    }

    public Optional<Handler<O>> createHandler(World world, UUID uuid) {
        return this.createHandler(this.getUUIDFile(world, uuid));
    }

    public Optional<Handler<O>> createHandler(File file) {
        try {
            return Optional.ofNullable(this.fileDeseralizer.apply(new FileInputStream(file))).map(o -> new Handler<>(o, this));
        } catch (IOException e) {
            TabulaTaxidermy.getLogger().error("Unable to read file '" + file.getAbsolutePath() + "'", e);
        }
        return Optional.empty();
    }

    private Handler<O> createHandlerFromBuf(ByteBuf buf) {
        return new Handler<>(this.deseralizer.apply(buf), this);
    }

    public static void write(ByteBuf buf, DataHandler<?> handler) {
        buf.writeBoolean(handler == TEXTURE);
    }

    public static DataHandler<?> read(ByteBuf buf) {
        return buf.readBoolean() ? TEXTURE : MODEL;
    }

    public static <O> void writeHandler(ByteBuf buf, Handler<O> handler) {
        write(buf, handler.parent);
        handler.parent.seralizer.accept(buf, handler.object);
    }

    public static Handler<?> readHandler(ByteBuf buf) {
        return read(buf).createHandlerFromBuf(buf);
    }

    public static class Handler<O> {
        final O object;
        @Getter
        final DataHandler<O> parent;

        private Handler(O object, DataHandler<O> handler) {
            this.object = object;
            this.parent = handler;
        }

        public void applyTo(TTBlockEntity blockEntity, UUID uuid) {
            this.parent.applyTo(blockEntity, uuid);
        }

        public void saveToFile(World world, UUID uuid, String name, String uploader) {
            File file = this.parent.getUUIDFile(world, uuid);
            try {
                this.parent.filsSeralizer.accept(this.object, new FileOutputStream(file));
            } catch (IOException e) {
                TabulaTaxidermy.getLogger().error("Unable to write object type '" + this.parent.typeName + "' to " + file.getAbsolutePath(), e);
            }

            this.parent.appendJsonFile(world, uuid, name, uploader);
        }

    }

    private interface ThrowableBiConsumer<T, U>  { void accept(T t, U u) throws IOException; }
    private interface ThrowableFunction<T, R>  { R apply(T t) throws IOException; }
}
