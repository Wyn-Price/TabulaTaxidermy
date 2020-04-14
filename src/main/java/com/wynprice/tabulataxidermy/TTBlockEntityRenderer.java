package com.wynprice.tabulataxidermy;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;

public class TTBlockEntityRenderer extends TileEntitySpecialRenderer<TTBlockEntity> {
    @Override
    public void render(TTBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        Vector3f t = te.getTranslation();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.translate(t.x, t.y, t.z);
        Vector3f r = te.getRotation();
        if(r.y != 0) {
            GlStateManager.rotate(r.y, 0, 1, 0);
        }
        if(r.x != 0) {
            GlStateManager.rotate(r.x, 1, 0, 0);
        }
        if(r.z != 0) {
            GlStateManager.rotate(r.z, 0, 0, 1);
        }
        GlStateManager.scale(te.getScale(), te.getScale(), te.getScale());
        GlStateManager.translate(0, -1.5, 0);

        TabulaModel model = te.getModel();
        ResourceLocation location = te.getTexture();

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
        } else {
            TTClientCache.MODEL.get(te.getModelUUID()).ifPresent(te::setModel);
            TTClientCache.TEXTURE.get(te.getTextureUUID()).ifPresent(te::setTexture);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TTBlockEntity te) {
        return true;
    }
}
