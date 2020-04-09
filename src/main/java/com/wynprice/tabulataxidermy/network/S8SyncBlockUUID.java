package com.wynprice.tabulataxidermy.network;

import com.wynprice.tabulataxidermy.TTBlockEntity;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.network.WorldModificationsMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class S8SyncBlockUUID implements IMessage {

    private BlockPos pos;
    private UUID uuid;

    public S8SyncBlockUUID() {
    }

    public S8SyncBlockUUID(BlockPos pos, UUID uuid) {
        this.pos = pos;
        this.uuid = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.uuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
    }

    public static class Handler extends WorldModificationsMessageHandler<S8SyncBlockUUID, IMessage> {

        @Override
        protected void handleMessage(S8SyncBlockUUID message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.pos);
            if(entity instanceof TTBlockEntity) {
                ((TTBlockEntity) entity).setDataUUID(message.uuid);
            }
        }
    }
}
