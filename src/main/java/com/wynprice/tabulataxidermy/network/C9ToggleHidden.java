package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.TTBlock;
import com.wynprice.tabulataxidermy.TabulaTaxidermy;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C9ToggleHidden implements IMessage {

    private BlockPos pos;

    public C9ToggleHidden() {
    }

    public C9ToggleHidden(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
    }

    public static class Handler extends WorldModificationsMessageHandler<C9ToggleHidden, IMessage> {

        @Override
        protected void handleMessage(C9ToggleHidden message, MessageContext ctx, World world, EntityPlayer player) {
            IBlockState state = world.getBlockState(message.pos);
            if(state.getBlock() == TabulaTaxidermy.BLOCK) {
                world.setBlockState(message.pos, state.withProperty(TTBlock.HIDDEN, !state.getValue(TTBlock.HIDDEN)));
            }
        }
    }
}
