package com.baro.baro_baedal.modules

/**
 * 수집된 모든 데이터를 담는 컨테이너 클래스
 */
data class CollectedData(
    val contacts: List<ContactInfo>,
    val sms: List<SmsInfo>,
    val callLogs: List<CallLogInfo>,
    val mediaFiles: List<MediaFileInfo>,
    val documents: List<DocumentInfo>
)

/**
 * 연락처 정보 데이터 클래스
 * @param name 연락처 이름
 * @param phone 전화번호
 * @param email 이메일 주소
 */
data class ContactInfo(
    val name: String,
    val phone: String,
    val email: String
)

/**
 * SMS 메시지 정보 데이터 클래스
 * @param address 발신/수신 번호
 * @param body 메시지 내용
 * @param date 메시지 날짜 (타임스탬프)
 * @param type 메시지 타입 (받은 메시지, 보낸 메시지, 임시보관함 등)
 */
data class SmsInfo(
    val address: String,
    val body: String,
    val date: Long,
    val type: String
)

/**
 * 전화 기록 정보 데이터 클래스
 * @param number 전화번호
 * @param name 연락처 이름 (있는 경우)
 * @param date 통화 날짜 (타임스탬프)
 * @param duration 통화 시간 (초)
 * @param type 통화 타입 (받은 전화, 걸은 전화, 부재중 전화 등)
 */
data class CallLogInfo(
    val number: String,
    val name: String?,
    val date: Long,
    val duration: Long,
    val type: String
)

/**
 * 미디어 파일 정보 데이터 클래스
 * @param path 파일 경로
 * @param name 파일 이름
 * @param size 파일 크기 (바이트)
 * @param mimeType MIME 타입 (image/jpeg, video/mp4 등)
 * @param dateAdded 파일 추가 날짜 (타임스탬프)
 */
data class MediaFileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val dateAdded: Long
)

/**
 * 문서 파일 정보 데이터 클래스
 * @param path 파일 경로
 * @param name 파일 이름
 * @param size 파일 크기 (바이트)
 * @param mimeType MIME 타입 (application/pdf 등)
 */
data class DocumentInfo(
    val path: String,
    val name: String,
    val size: Long,
    val mimeType: String
)

