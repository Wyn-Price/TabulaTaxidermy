package com.wynprice.tabulataxidermy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynprice.tabulataxidermy.network.C1RequestDataForUUID;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class TTClientCache<F, T> {
    public static final TTClientCache<TabulaModelInformation, TabulaModel> MODEL = new TTClientCache<>(DataHandler.MODEL, TabulaModelInformation::createModel);

    public static final TTClientCache<BufferedImage, ResourceLocation> TEXTURE = new TTClientCache<>(DataHandler.TEXTURE, image ->
        Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(TabulaTaxidermy.MODID, new DynamicTexture(image))
    );

    private final DataHandler<F> handler;
    private final Function<F, T> creator;

    final Map<UUID, T> map = new HashMap<>();
    private final Set<UUID> requested = new HashSet<>();

    public Optional<T> get(UUID uuid) {
        if(uuid.getLeastSignificantBits() == 0 && uuid.getMostSignificantBits() == 0) {
            return Optional.empty();
        }
        if(this.map.containsKey(uuid)) {
            return Optional.of(this.map.get(uuid));
        }
        if(!this.requested.contains(uuid)) {
            TabulaTaxidermy.NETWORK.sendToServer(new C1RequestDataForUUID(uuid, this.handler));
            this.requested.add(uuid);
        }
        return Optional.empty();
    }

    public static <O, F> void handle(UUID uuid, DataHandler.Handler<O> handler) {
        @SuppressWarnings("unchecked")
        TTClientCache<O, F> cache = (TTClientCache<O, F>) (handler.parent == DataHandler.TEXTURE ? TEXTURE : MODEL);
        cache.map.put(uuid, cache.creator.apply(handler.object));

    }
}
