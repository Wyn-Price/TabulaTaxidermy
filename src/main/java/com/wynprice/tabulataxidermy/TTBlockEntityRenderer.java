package com.wynprice.tabulataxidermy;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;

public class TTBlockEntityRenderer extends TileEntitySpecialRenderer<TTBlockEntity> {
    @Override
    public void render(TTBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        TabulaModel model = te.getClientModelCache();
        ResourceLocation location = te.getClientTextureCache();

        if(model != null && location != null) {
            Map<String, Vector3f> poseData = te.getPoseData();
            for(TabulaModelRenderer box : model.getAllCubes()) {
                Vector3f rotations = poseData.get(box.boxName);
                if(rotations != null) {
                    box.rotateAngleX = rotations.x;
                    box.rotateAngleY = rotations.y;
                    box.rotateAngleZ = rotations.z;
                } else {
                    box.resetRotations();
                }
            }

            Minecraft.getMinecraft().renderEngine.bindTexture(location);
            model.renderBoxes(1/16F);
        } else if(te.getDataUUID() != null) {
            TTClientCache.INSTANCE.get(te.getDataUUID()).ifPresent(p -> {
                te.setClientTextureCache(p.getLeft());
                te.setClientModelCache(p.getRight());
            });
        }

        GlStateManager.popMatrix();
    }
}
