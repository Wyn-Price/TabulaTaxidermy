package com.wynprice.tabulataxidermy;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class TTBlockEntity extends TaxidermyBlockEntity {

    @Getter @Setter
    private UUID dataUUID = new UUID(0, 0);

    @Getter @Setter
    private ResourceLocation clientTextureCache;

    @Getter @Setter
    private TabulaModel clientModelCache;

    @Override
    public ResourceLocation getTexture() {
        return this.clientTextureCache;
    }

    @Override
    public TabulaModel getModel() {
        return this.clientModelCache;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.dataUUID = compound.getUniqueId("data");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setUniqueId("data", this.dataUUID);
        return super.writeToNBT(compound);
    }
}
