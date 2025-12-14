package draylar.staffofbuilding.fabric.network;

import draylar.staffofbuilding.fabric.StaffOfBuilding;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UndoMessage() implements CustomPacketPayload {
    public static final Type<UndoMessage> ID = new Type<>(StaffOfBuilding.id("undo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UndoMessage> CODEC = StreamCodec.unit(new UndoMessage());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
