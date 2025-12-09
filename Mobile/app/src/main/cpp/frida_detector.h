extern "C" {
JNIEXPORT jboolean JNICALL Java_com_baro_baro_1baedal_modules_security_FridaNative_checkFridaTrampoline(JNIEnv *, jobject);
JNIEXPORT jboolean JNICALL Java_com_baro_baro_1baedal_modules_security_FridaNative_checkFridaServer(JNIEnv *, jobject);
JNIEXPORT jboolean JNICALL Java_com_baro_baro_1baedal_modules_security_FridaNative_scanSuspiciousMemory(JNIEnv *, jobject);
}