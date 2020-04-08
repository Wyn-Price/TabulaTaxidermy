package com.wynprice.tabulataxidermy;

import com.wynprice.tabulataxidermy.network.C1RequestDataForUUID;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import sun.plugin.util.UIUtil;

import java.awt.image.BufferedImage;
import java.util.*;

public enum TTClientCache {
    INSTANCE;

    private final Map<UUID, TabulaModel> modelInfo = new HashMap<>();
    private final Map<UUID, ResourceLocation> imageMap = new HashMap<>();

    private final Set<UUID> requested = new HashSet<>();

    public void put(UUID uuid, TabulaModelInformation info, BufferedImage image) {
        this.modelInfo.put(uuid, info.createModel());

        DynamicTexture texture = new DynamicTexture(image);
        this.imageMap.put(uuid, Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(TabulaTaxidermy.MODID, texture));
    }

    public Optional<Pair<ResourceLocation, TabulaModel>> get(UUID uuid) {
        if(this.modelInfo.containsKey(uuid)) {
            return Optional.of(Pair.of(this.imageMap.get(uuid), this.modelInfo.get(uuid)));
        }
        if(!this.requested.contains(uuid)) {
            TabulaTaxidermy.NETWORK.sendToServer(new C1RequestDataForUUID(uuid));
            this.requested.add(uuid);
        }
        return Optional.empty();
    }
}
