package com.wynprice.taxidermy;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;

import java.util.UUID;

@Getter
@Setter
public class TaxidermyBlockEntity extends BaseTaxidermyBlockEntity {

    private UUID modelUUID = new UUID(0, 0);
    private UUID textureUUID = new UUID(0, 0);

    private Vector3f translation = new Vector3f();
    private Vector3f rotation = new Vector3f();

    private float scale = 1F;

    private ResourceLocation texture;
    private DCMModel model;

    public TaxidermyBlockEntity() {
        super(Taxidermy.BLOCK_ENTITY.get());
    }

    public void setModelUUID(UUID modelUUID) {
        this.modelUUID = modelUUID;
        if(this.level != null && this.level.isClientSide) {
            this.model = null;
        }
    }

    public void setTextureUUID(UUID textureUUID) {
        this.textureUUID = textureUUID;
        if(this.level != null && this.level.isClientSide) {
            this.texture = null;
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        this.setModelUUID(compound.getUUID("Model"));
        this.setTextureUUID(compound.getUUID("Texture"));

        this.translation = new Vector3f(compound.getFloat("TranslationX"), compound.getFloat("TranslationY"), compound.getFloat("TranslationZ"));
        this.rotation = new Vector3f(compound.getFloat("RotationX"), compound.getFloat("RotationY"), compound.getFloat("RotationZ"));
        this.scale = compound.getFloat("Scale");
        super.load(state, compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putUUID("Model", this.modelUUID);
        nbt.putUUID("Texture", this.textureUUID);

        nbt.putFloat("TranslationX", this.translation.x());
        nbt.putFloat("TranslationY", this.translation.y());
        nbt.putFloat("TranslationZ", this.translation.z());

        nbt.putFloat("RotationX", this.rotation.x());
        nbt.putFloat("RotationY", this.rotation.y());
        nbt.putFloat("RotationZ", this.rotation.z());

        nbt.putFloat("Scale", this.scale);
        return super.save(nbt);
    }

//    @Override
//    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
//        return oldState.getBlock() != newSate.getBlock();
//    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getViewDistance() {
        return Long.MAX_VALUE;
    }

    public void setProperty(int index, float value) {
        if(index >= 0 && index < 3) {
            float[] arr = this.get(this.translation);
            arr[index] = value;
            this.translation.set(arr);
        } else if(index >= 3 && index < 6) {
            float[] arr = this.get(this.rotation);
            arr[index - 3] = value;
            this.rotation.set(arr);
        } else if(index == 6) {
            this.scale = value;
        }
    }

    private float[] get(Vector3f vec) {
        return new float[]{ vec.x(), vec.y(), vec.z() };
    }
}
