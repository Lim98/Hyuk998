package com.baro.baro_baedal.modules

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

/**
 * 연락처 정보를 수집하는 클래스
 * 사용자의 연락처에서 이름, 전화번호, 이메일 주소를 추출합니다.
 */
class ContactCollector(private val context: Context) {
    
    /**
     * 모든 연락처 정보를 수집합니다.
     * READ_CONTACTS 권한이 필요합니다.
     * 
     * @return 연락처 정보 리스트 (전화번호 기준으로 중복 제거)
     */
    fun collect(): List<ContactInfo> {
        val contacts = mutableListOf<ContactInfo>()
        
        // 권한 체크
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            return contacts
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        
        // 전화번호가 있는 연락처만 조회
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                val phone = it.getString(phoneIndex) ?: ""
                val contactId = it.getLong(contactIdIndex)
                
                // 이메일은 별도 쿼리로 조회
                val email = getContactEmail(contactId)
                
                contacts.add(ContactInfo(name, phone, email))
            }
        }
        
        // 전화번호 기준으로 중복 제거 (한 연락처에 여러 전화번호가 있을 수 있음)
        return contacts.distinctBy { it.phone }
    }
    
    /**
     * 특정 연락처 ID의 이메일 주소를 조회합니다.
     * 
     * @param contactId 연락처 ID
     * @return 이메일 주소 (없으면 빈 문자열)
     */
    private fun getContactEmail(contactId: Long): String {
        val emailCursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
        
        emailCursor?.use {
            if (it.moveToFirst()) {
                val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                return it.getString(emailIndex) ?: ""
            }
        }
        return ""
    }
}

