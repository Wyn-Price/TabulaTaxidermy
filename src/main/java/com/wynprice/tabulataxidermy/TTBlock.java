package com.wynprice.tabulataxidermy;

import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TTBlock extends Block {

    public static final PropertyBool HIDDEN = PropertyBool.create("hidden");

    public TTBlock(Material materialIn) {
        super(materialIn);
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(HIDDEN, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if(worldIn.isRemote && entity instanceof TTBlockEntity) {
            SidedExecutor.runClient(() -> () -> Minecraft.getMinecraft().displayGuiScreen(new GuiTTBlock((TTBlockEntity) entity)));
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HIDDEN);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return !state.getValue(HIDDEN) && super.canRenderInLayer(state, layer);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TTBlockEntity();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HIDDEN) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HIDDEN, meta == 1);
    }
}
