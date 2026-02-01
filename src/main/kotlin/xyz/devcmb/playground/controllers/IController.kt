package xyz.devcmb.playground.controllers

import org.bukkit.event.Listener

interface IController: Listener {
    fun init()
    fun cleanup() {}
}