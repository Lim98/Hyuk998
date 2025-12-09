package com.baro.baro_baedal.modules

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog

/**
 * 전화 기록을 수집하는 클래스
 * 사용자의 통화 기록(발신, 수신, 부재중)을 추출합니다.
 * READ_CALL_LOG 권한이 필요합니다.
 */
class CallLogCollector(private val context: Context) {
    
    /**
     * 최근 전화 기록을 수집합니다.
     * 최대 100개의 통화 기록을 날짜 내림차순으로 수집합니다.
     * 
     * @return 전화 기록 정보 리스트
     */
    fun collect(): List<CallLogInfo> {
        val callLogs = mutableListOf<CallLogInfo>()
        
        // 권한 체크
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_CALL_LOG)) {
            return callLogs
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        
        // 최근 통화 기록 100개를 날짜 내림차순으로 조회
        // ContentResolver는 LIMIT을 지원하지 않으므로 정렬만 하고, 개수는 코드에서 제한
        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )
        
        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            
            var count = 0
            val maxCount = 100
            
            while (it.moveToNext() && count < maxCount) {
                val number = it.getString(numberIndex) ?: ""
                val name = it.getString(nameIndex) // 연락처에 저장된 이름 (없을 수 있음)
                val date = it.getLong(dateIndex)
                val duration = it.getLong(durationIndex)
                
                // 통화 타입을 한글로 변환
                val type = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> "받은 전화"
                    CallLog.Calls.OUTGOING_TYPE -> "걸은 전화"
                    CallLog.Calls.MISSED_TYPE -> "부재중 전화"
                    CallLog.Calls.VOICEMAIL_TYPE -> "음성사서함"
                    CallLog.Calls.REJECTED_TYPE -> "거절한 전화"
                    CallLog.Calls.BLOCKED_TYPE -> "차단된 전화"
                    else -> "기타"
                }
                
                callLogs.add(CallLogInfo(number, name, date, duration, type))
                count++
            }
        }
        
        return callLogs
    }
}

