package com.wynprice.taxidermy;

import com.wynprice.taxidermy.network.C2SRequestDataForUUID;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.utils.DummyImage;
import net.dumbcode.studio.model.ModelInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class TaxidermyClientCache<F, T> {
    public static final TaxidermyClientCache<ModelInfo, DCMModel> MODEL = new TaxidermyClientCache<>(DataHandler.MODEL, DCMModel::new);

    public static final TaxidermyClientCache<DummyImage, ResourceLocation> TEXTURE = new TaxidermyClientCache<>(DataHandler.TEXTURE, image ->
        Minecraft.getInstance().textureManager.register(Taxidermy.MODID, new DynamicTexture(image.getImage()))
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
            Taxidermy.NETWORK.sendToServer(new C2SRequestDataForUUID(uuid, this.handler));
            this.requested.add(uuid);
        }
        return Optional.empty();
    }

    public static <O, F> void handle(UUID uuid, DataHandler.Handler<O> handler) {
        @SuppressWarnings("unchecked")
        TaxidermyClientCache<O, F> cache = (TaxidermyClientCache<O, F>) (handler.parent == DataHandler.TEXTURE ? TEXTURE : MODEL);
        cache.map.put(uuid, cache.creator.apply(handler.object));

    }
}
