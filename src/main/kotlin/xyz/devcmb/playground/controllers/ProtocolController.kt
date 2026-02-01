package xyz.devcmb.playground.controllers

import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.protocol.HardcoreHeartsAdapter

class ProtocolController : IController {
    override fun init() {
        ParkourPlayground.protocolManager.addPacketListener(HardcoreHeartsAdapter())
    }
}