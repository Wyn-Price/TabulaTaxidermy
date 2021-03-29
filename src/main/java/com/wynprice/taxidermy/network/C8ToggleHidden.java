package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlock;
import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class C8ToggleHidden {

    private BlockPos pos;

    public static C8ToggleHidden fromBytes(PacketBuffer buf) {
        return new C8ToggleHidden(buf.readBlockPos());
    }

    public static void toBytes(C8ToggleHidden packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(C8ToggleHidden message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = context.getSender().level;
            BlockState state = world.getBlockState(message.pos);
            if(state.getBlock() == Taxidermy.BLOCK.get()) {
                world.setBlock(message.pos, state.setValue(TaxidermyBlock.HIDDEN, !state.getValue(TaxidermyBlock.HIDDEN)), 3);
            }
        });
        context.setPacketHandled(true);
    }
}
