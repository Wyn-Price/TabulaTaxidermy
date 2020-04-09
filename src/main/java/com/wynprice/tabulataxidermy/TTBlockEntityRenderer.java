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
        GlStateManager.translate(x + 0.5 + t.x, y + 0.5 + t.y, z + 0.5 + t.z);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.translate(0, -1, 0);

        Vector3f r = te.getRotation();
        if(r.z != 0) {
            GlStateManager.rotate(r.z, 0, 0, 1);
        }
        if(r.y != 0) {
            GlStateManager.rotate(r.y, 0, 1, 0);
        }
        if(r.x != 0) {
            GlStateManager.rotate(r.x, 1, 0, 0);
        }

        GlStateManager.scale(te.getScale(), te.getScale(), te.getScale());

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
        } else if(te.getDataUUID() != null) {
            TTClientCache.INSTANCE.get(te.getDataUUID()).ifPresent(p -> {
                te.setTexture(p.getLeft());
                te.setModel(p.getRight());
            });
        }

        GlStateManager.popMatrix();
    }
}
