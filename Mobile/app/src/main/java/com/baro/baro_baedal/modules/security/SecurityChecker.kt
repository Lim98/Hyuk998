package com.baro.baro_baedal.modules.security

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.File

suspend fun quickChecks(context: Context): String? = withContext(Dispatchers.IO) {

    // 1) Debugger check
    if (android.os.Debug.isDebuggerConnected()) {
        Log.w(TAG, "Debugger connected")
        return@withContext "Debugger Detected"
    }

    // 2) Known frida files
    val suspectFiles = listOf(
//      "/data/local/tmp/frida-server",
//        "/data/local/tmp/frida-gadget.so",
//        "/data/local/tmp/re.frida.server"
        "testsetste"
    )
    for (path in suspectFiles) {
        if (File(path).exists()) {
            Log.w(TAG, "Frida file found: $path")
            return@withContext "Frida File Detected: $path"
        }
    }

    // 3) Native checks
    if (FridaNative.checkFridaTrampoline()) {
        return@withContext "libc 함수 시작 코드 변조 됐단다"
    }

    if (FridaNative.checkFridaServer()) {
        return@withContext "TCP 27042/27043 포트가 열렸단다"
    }

    if (FridaNative.scanSuspiciousMemory()) {
//        return@withContext "RWX 영역 존재하거나 Frida 가젯 라이브러리가 존재한단다"
    }

    return@withContext null // 정상
}

fun startChecks(context: Context, onDetected: (reason: String) -> Unit) {
    CoroutineScope(Dispatchers.Default).launch {
        val reason = quickChecks(context)

        if (reason != null) {
            withContext(Dispatchers.Main) {
                onDetected(reason)
            }
        }
    }
}