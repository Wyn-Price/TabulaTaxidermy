package com.wynprice.taxidermy;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;

public class TaxidermyBlockEntityRenderer extends TileEntitySpecialRenderer<TaxidermyBlockEntity> {
    @Override
    public void render(TaxidermyBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
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

        if(model != null) {
            if(location != null) {
                Map<String, TaxidermyHistory.CubeProps> poseData = te.getPoseData();
                for(TabulaModelRenderer box : model.getAllCubes()) {
                    TaxidermyHistory.CubeProps cubeProps = poseData.get(box.boxName);
                    if(cubeProps != null) {
                        cubeProps.applyTo(box);
                    } else {
                        box.resetRotations();
                    }
                }
                Minecraft.getMinecraft().renderEngine.bindTexture(location);
                model.renderBoxes(1/16F);
            } else {
                model.resetAnimations();
                TaxidermyClientCache.TEXTURE.get(te.getTextureUUID()).ifPresent(te::setTexture);
            }
        } else {
            TaxidermyClientCache.MODEL.get(te.getModelUUID()).ifPresent(te::setModel);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TaxidermyBlockEntity te) {
        return true;
    }
}
