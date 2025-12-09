package com.baro.baro_baedal.modules

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * 권한 체크를 위한 유틸리티 클래스
 */
object PermissionHelper {
    
    /**
     * 저장소 접근 권한이 있는지 확인
     * Android 13 (API 33) 이상에서는 미디어별 권한을 확인하고,
     * 그 이하 버전에서는 READ_EXTERNAL_STORAGE 권한을 확인합니다.
     * 
     * @param context Context 객체
     * @return 권한이 있으면 true, 없으면 false
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : 미디어별 세분화된 권한
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 이하 : 일반 저장소 권한
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 특정 권한이 있는지 확인
     * 
     * @param context Context 객체
     * @param permission 확인할 권한 (예: Manifest.permission.READ_CONTACTS)
     * @return 권한이 있으면 true, 없으면 false
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

