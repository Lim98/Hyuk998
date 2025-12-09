package com.baro.baro_baedal

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.baro.baro_baedal.modules.security.startChecks
import com.baro.baro_baedal.ui.theme.BarobaedalTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //RetrofitClient.init(applicationContext)
        startChecks(this) { reason ->
            AlertDialog.Builder(this)
                .setTitle("보안 감지")
                .setMessage("현재 기기에서 보안 위협이 감지되었습니다.\n\n사유: $reason")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        enableEdgeToEdge()
        setContent {
            BarobaedalTheme {
                AppNavigator()
            }
        }
    }
}

