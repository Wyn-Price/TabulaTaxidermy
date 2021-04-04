package com.wynprice.taxidermy;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.DCMBufferHandler;
import net.dumbcode.dumblibrary.server.utils.ImageBufferHandler;
import net.dumbcode.studio.model.*;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DataHandler<O> {
    private static final FolderName TAXIDERMY_STORAGE = new FolderName("taxidermy_storage");
    public static final DataHandler<NativeImage> TEXTURE = new DataHandler<>(
        "texture", "png", "Texture File",TaxidermyBlockEntity::setTextureUUID, ImageBufferHandler.INSTANCE,
        (img, stream) -> stream.write(img.asByteArray()),
        (stream, fromFile) -> NativeImage.read(stream)
    );

    public static final DataHandler<ModelInfo> MODEL = new DataHandler<>(
        "model", "dcm", "Model File", TaxidermyBlockEntity::setModelUUID, DCMBufferHandler.INSTANCE,
        ModelWriter::writeModel,
        (stream, fromFile) -> ModelLoader.loadModel(stream, RotationOrder.ZYX, fromFile ? ModelMirror.YZ : ModelMirror.NONE)
    );

    public static final DataHandler[] HANDLERS = { TEXTURE, MODEL };

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter @Setter
    private String folderCache;

    @Getter
    private final String typeName;
    @Getter
    private final String extension;
    @Getter
    private final String fileDescription;
    private final BiConsumer<TaxidermyBlockEntity, UUID> uuidSetter;

    //IO Stuff:
    private final BiConsumer<PacketBuffer, O> seralizer;
    private final Function<PacketBuffer, O> deseralizer;
    private final ThrowableBiConsumer<O, OutputStream> filsSeralizer;
    private final ThrowableBiFunction<InputStream, Boolean, O> fileDeseralizer;

    public <T extends BiConsumer<PacketBuffer, O> & Function<PacketBuffer, O>> DataHandler(
        String typeName, String extension, String fileDescription, BiConsumer<TaxidermyBlockEntity, UUID> uuidSetter, T handler,
        ThrowableBiConsumer<O, OutputStream> filsSeralizer, ThrowableBiFunction<InputStream, Boolean, O> fileDeseralizer
    ) {
        this.typeName = typeName;
        this.extension = extension;
        this.fileDescription = fileDescription;
        this.uuidSetter = uuidSetter;
        this.seralizer = handler;
        this.deseralizer = handler;
        this.filsSeralizer = filsSeralizer;
        this.fileDeseralizer = fileDeseralizer;
    }

    public Path getBaseFolder() {
        Path path = ServerLifecycleHooks.getCurrentServer().getWorldPath(TAXIDERMY_STORAGE).resolve(this.typeName);
        if(!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //Maybe don't throw exception?
                throw new IllegalArgumentException("Unable to create base folder at file '" + path.toAbsolutePath() + "'", e);
            }
        }
        return path;
    }

    private Path getUUIDFile(UUID uuid) {
        return this.getBaseFolder().resolve(uuid.toString().replaceAll("-", "") + "." + this.extension);
    }

    private Path getJsonFile() {
        return this.getBaseFolder().resolve("data.json");
    }

    public List<DataHeader> getHeaders() {
        Path path = this.getJsonFile();

        List<DataHeader> headers = new ArrayList<>();
        try {
            if(Files.exists(path)) {
                JsonParser parser = new JsonParser();
                for (Map.Entry<String, JsonElement> entry : parser.parse(Files.newBufferedReader(path)).getAsJsonObject().entrySet()) {
                    JsonObject json = entry.getValue().getAsJsonObject();
                    headers.add(new DataHeader(UUID.fromString(entry.getKey()), JSONUtils.getAsString(json, "name"), JSONUtils.getAsString(json, "uploader")));
                }
            }
        } catch (IOException e) {
            Taxidermy.getLogger().error("Unable to read data file at '" + path.toAbsolutePath() + "'", e);
        }
        return headers;
    }

    private void writeHeaders(List<DataHeader> headers) {
        Path path = this.getJsonFile();
        JsonObject json = new JsonObject();
        for (DataHeader header : headers) {
            JsonObject obj = new JsonObject();
            json.add(header.getUuid().toString(), obj);

            obj.addProperty("name", header.getName());
            obj.addProperty("uploader", header.getUploader());
        }

        try {
            Files.write(path, Collections.singleton(GSON.toJson(json)));
        } catch (IOException e) {
            Taxidermy.getLogger().error("Unable to write data file at '" + path.toAbsolutePath() + "'", e);
        }
    }

    public void appendJsonFile(UUID uuid, String name, String uploader) {
        List<DataHeader> headers = this.getHeaders();
        headers.add(new DataHeader(uuid, name, uploader));
        this.writeHeaders(headers);
    }

    public void applyTo(TaxidermyBlockEntity blockEntity, UUID uuid) {
        this.uuidSetter.accept(blockEntity, uuid);
    }

    public Optional<Handler<O>> createHandler(UUID uuid) {
        return this.createHandler(this.getUUIDFile(uuid), false);
    }

    public Optional<Handler<O>> createHandler(Path file, boolean userPath) {
        try {
            return Optional.ofNullable(this.fileDeseralizer.apply(Files.newInputStream(file), userPath)).map(o -> new Handler<>(o, this));
        } catch (IOException e) {
            Taxidermy.getLogger().error("Unable to read file '" + file.toAbsolutePath() + "'", e);
        }
        return Optional.empty();
    }

    private Handler<O> createHandlerFromBuf(PacketBuffer buf) {
        return new Handler<>(this.deseralizer.apply(buf), this);
    }

    public static void write(PacketBuffer buf, DataHandler<?> handler) {
        buf.writeBoolean(handler == TEXTURE);
    }

    public static DataHandler<?> read(PacketBuffer buf) {
        return buf.readBoolean() ? TEXTURE : MODEL;
    }

    public static <O> void writeHandler(PacketBuffer buf, Handler<O> handler) {
        write(buf, handler.parent);
        handler.parent.seralizer.accept(buf, handler.object);
    }

    public static Handler<?> readHandler(PacketBuffer buf) {
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

        public void applyTo(TaxidermyBlockEntity blockEntity, UUID uuid) {
            this.parent.applyTo(blockEntity, uuid);
        }

        public void saveToFile(UUID uuid, String name, String uploader) {
            Path path = this.parent.getUUIDFile(uuid);
            try {
                this.parent.filsSeralizer.accept(this.object, Files.newOutputStream(path));
            } catch (IOException e) {
                Taxidermy.getLogger().error("Unable to write object type '" + this.parent.typeName + "' to " + path.toAbsolutePath(), e);
            }

            this.parent.appendJsonFile(uuid, name, uploader);
        }

    }

    private interface ThrowableBiConsumer<T, U>  { void accept(T t, U u) throws IOException; }
    private interface ThrowableBiFunction<T, A, R>  { R apply(T t, A a) throws IOException; }
}
