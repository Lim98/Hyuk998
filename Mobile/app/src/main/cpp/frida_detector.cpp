#include <jni.h>
#include <string>
#include <fstream>
#include <unistd.h>
#include <sys/mman.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  "FRIDA-DETECT", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  "FRIDA-DETECT", __VA_ARGS__)

// Inline hook 패턴 탐지
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_baro_baro_1baedal_modules_security_FridaNative_checkFridaTrampoline(JNIEnv *env, jobject thiz) {
    void* handle = dlopen("libc.so", RTLD_NOW);
    if (!handle) return JNI_FALSE;

    void* open_sym = dlsym(handle, "open");
    if (!open_sym) return JNI_FALSE;

    unsigned char* op = (unsigned char*) open_sym;
    if (op[0] == 0xFF || op[0] == 0x00 || op[0] == 0x20) {
        LOGW("Inline hook suspicious at open(): %02X %02X %02X %02X",
             op[0], op[1], op[2], op[3]);
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

// Frida 서버 포트 탐지
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_baro_baro_1baedal_modules_security_FridaNative_checkFridaServer(JNIEnv *env, jobject thiz) {
    std::ifstream fs("/proc/net/tcp");
    if (!fs.is_open()) return JNI_FALSE;

    std::string line;
    while (std::getline(fs, line)) {
        if (line.find("6A6A") != std::string::npos || line.find("6A6B") != std::string::npos) {
            LOGW("Frida server port found in /proc/net/tcp");
            return JNI_TRUE;
        }
    }

    return JNI_FALSE;
}

// 의심스러운 메모리 영역 탐지
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_baro_baro_1baedal_modules_security_FridaNative_scanSuspiciousMemory(JNIEnv *env, jobject thiz) {
    std::ifstream fs("/proc/self/maps");
    if (!fs.is_open()) return JNI_FALSE;

    std::string line;
    while (std::getline(fs, line)) {
        if (line.find("rwxp") != std::string::npos && line.find("anon") != std::string::npos) {
            LOGW("Suspicious RWX memory detected: %s", line.c_str());
            return JNI_TRUE;
        }

        if (line.find("frida") != std::string::npos || line.find("gadget") != std::string::npos) {
            LOGW("Frida/gadget library found");
            return JNI_TRUE;
        }
    }

    return JNI_FALSE;
}