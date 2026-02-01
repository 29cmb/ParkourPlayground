package xyz.devcmb.playground.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import xyz.devcmb.playground.ParkourPlayground

class HardcoreHeartsAdapter : PacketAdapter(
    ParkourPlayground.plugin,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.LOGIN
) {
    override fun onPacketSending(event: PacketEvent?) {
        if(event!!.packetType.equals(PacketType.Play.Server.LOGIN)) {
            event.packet.booleans.write(0, true)
        }
    }
}