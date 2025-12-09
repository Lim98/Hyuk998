package com.baro.baro_baedal.modules.security


object FridaNative {
    init {
        System.loadLibrary("frida-native")
    }

    external fun checkFridaTrampoline(): Boolean
    external fun checkFridaServer(): Boolean
    external fun scanSuspiciousMemory(): Boolean
}