package ru.zundonza.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zundonza.network.PacketLogger;

@Mixin(ClientConnection.class)
public class PacketEventMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketLogger.log(packet, PacketLogger.Direction.INCOMING);
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"))
    public void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        PacketLogger.log(packet, PacketLogger.Direction.OUTGOING);
    }

}
