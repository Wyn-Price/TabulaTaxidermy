package com.wynprice.taxidermy.network;

import com.wynprice.taxidermy.Taxidermy;
import com.wynprice.taxidermy.TaxidermyBlock;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SToggleHidden {

    public static C2SToggleHidden fromBytes(PacketBuffer buf) {
        return new C2SToggleHidden();
    }

    public static void toBytes(C2SToggleHidden packet, PacketBuffer buf) {
    }

    public static void handle(C2SToggleHidden message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity entity = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                BlockState state = entity.getBlockState();
                if (state.getBlock() == Taxidermy.BLOCK.get()) {
                    world.setBlock(entity.getBlockPos(), state.setValue(TaxidermyBlock.HIDDEN, !state.getValue(TaxidermyBlock.HIDDEN)), 3);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
