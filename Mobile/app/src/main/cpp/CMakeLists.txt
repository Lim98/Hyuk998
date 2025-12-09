cmake_minimum_required(VERSION 3.18.1)

# 라이브러리 이름과 소스 파일
add_library(
        frida-native            # Kotlin에서 loadLibrary("frida-native") 호출
        SHARED
        frida_detector.cpp
)

# NDK의 기본 라이브러리 연결
find_library(
        log-lib
        log
)

# 타겟 라이브러리 링크
target_link_libraries(
        frida-native
        ${log-lib}
        dl          # dlopen, dlsym 사용
)