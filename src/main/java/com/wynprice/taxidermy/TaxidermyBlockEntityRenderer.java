package com.wynprice.taxidermy;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Map;

public class TaxidermyBlockEntityRenderer extends TileEntityRenderer<TaxidermyBlockEntity> {

    public TaxidermyBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TaxidermyBlockEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        stack.pushPose();

        Vector3f t = te.getTranslation();
//        stack.mulPose(Vector3f.XP.rotationDegrees(180));
        stack.translate(t.x() + 0.5, t.y(), t.z() + 0.5);

        Vector3f r = te.getRotation();
        if(r.y() != 0) {
            stack.mulPose(Vector3f.YP.rotationDegrees(r.y()));
        }
        if(r.x() != 0) {
            stack.mulPose(Vector3f.XP.rotationDegrees(r.x()));
        }
        if(r.z() != 0) {
            stack.mulPose(Vector3f.ZP.rotationDegrees(r.z()));
        }

        stack.scale(te.getScale(), te.getScale(), te.getScale());

        DCMModel model = te.getModel();
        ResourceLocation location = te.getTexture();

        if(model != null) {
            if(location != null) {
                Map<String, TaxidermyHistory.CubeProps> poseData = te.getPoseData();
                for(DCMModelRenderer box : model.getAllCubes()) {
                    TaxidermyHistory.CubeProps cubeProps = poseData.get(box.getName());
                    if(cubeProps != null) {
                        cubeProps.applyTo(box);
                    } else {
                        box.resetRotations();
                    }
                }
                Minecraft.getInstance().textureManager.bind(location);
                model.renderBoxes(stack, combinedLightIn, location);
            } else {
                model.resetAnimations();
                TaxidermyClientCache.TEXTURE.get(te.getTextureUUID()).ifPresent(te::setTexture);
            }
        } else {
            TaxidermyClientCache.MODEL.get(te.getModelUUID()).ifPresent(te::setModel);
        }

        stack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TaxidermyBlockEntity te) {
        return true;
    }
}
