package com.wynprice.tabulataxidermy;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.UUID;

@Getter
@Setter
public class TTBlockEntity extends TaxidermyBlockEntity {

    private UUID modelUUID = new UUID(0, 0);
    private UUID textureUUID = new UUID(0, 0);

    private Vector3f translation = new Vector3f();
    private Vector3f rotation = new Vector3f();

    private float scale = 1F;

    private ResourceLocation texture;
    private TabulaModel model;

    public void setModelUUID(UUID modelUUID) {
        this.modelUUID = modelUUID;
        if(this.world != null && this.world.isRemote) {
            this.model = null;
        }
    }

    public void setTextureUUID(UUID textureUUID) {
        this.textureUUID = textureUUID;
        if(this.world != null && this.world.isRemote) {
            this.texture = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.setModelUUID(nbt.getUniqueId("Model"));
        this.setTextureUUID(nbt.getUniqueId("Texture"));

        this.translation = new Vector3f(nbt.getFloat("TranslationX"), nbt.getFloat("TranslationY"), nbt.getFloat("TranslationZ"));
        this.rotation = new Vector3f(nbt.getFloat("RotationX"), nbt.getFloat("RotationY"), nbt.getFloat("RotationZ"));
        this.scale = nbt.getFloat("Scale");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setUniqueId("Model", this.modelUUID);
        nbt.setUniqueId("Texture", this.textureUUID);

        nbt.setFloat("TranslationX", this.translation.x);
        nbt.setFloat("TranslationY", this.translation.z);
        nbt.setFloat("TranslationZ", this.translation.y);

        nbt.setFloat("RotationX", this.rotation.x);
        nbt.setFloat("RotationY", this.rotation.z);
        nbt.setFloat("RotationZ", this.rotation.y);

        nbt.setFloat("Scale", this.scale);

        return super.writeToNBT(nbt);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
