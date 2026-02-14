package xyz.devcmb.playground.controllers

import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.protocol.HardcoreHeartsAdapter

@Controller("protocolController", priority = Controller.Priority.LOW)
class ProtocolController : IController {
    override fun init() {
        ParkourPlayground.protocolManager.addPacketListener(HardcoreHeartsAdapter())
    }
}