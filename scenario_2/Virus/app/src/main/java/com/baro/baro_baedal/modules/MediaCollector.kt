package com.baro.baro_baedal.modules

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

/**
 * 미디어 파일(이미지, 비디오, 오디오)을 수집하는 클래스
 * 사용자의 갤러리 사진, 동영상, 음악 파일 정보를 추출합니다.
 * 저장소 접근 권한이 필요합니다.
 */
class MediaCollector(private val context: Context) {
    
    /**
     * 모든 미디어 파일(이미지, 비디오, 오디오)을 수집합니다.
     * 각 타입별로 최대 50개씩 수집합니다.
     * 
     * @return 미디어 파일 정보 리스트
     */
    fun collect(): List<MediaFileInfo> {
        val mediaFiles = mutableListOf<MediaFileInfo>()
        
        // 저장소 권한 체크
        if (!PermissionHelper.hasStoragePermission(context)) {
            return mediaFiles
        }
        
        // 이미지, 비디오, 오디오 파일 수집
        mediaFiles.addAll(collectImages())
        mediaFiles.addAll(collectVideos())
        mediaFiles.addAll(collectAudioFiles())
        
        return mediaFiles
    }
    
    /**
     * 이미지 파일을 수집합니다.
     * 최대 50개의 최근 이미지를 날짜 내림차순으로 수집합니다.
     * 
     * @return 이미지 파일 정보 리스트
     */
    private fun collectImages(): List<MediaFileInfo> {
        val images = mutableListOf<MediaFileInfo>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA
        )
        
        // ContentResolver는 LIMIT을 지원하지 않으므로 정렬만 하고, 개수는 코드에서 제한
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            
            var count = 0
            val maxCount = 50
            
            while (it.moveToNext() && count < maxCount) {
                val path = it.getString(dataIndex) ?: ""
                val name = it.getString(nameIndex) ?: ""
                val size = it.getLong(sizeIndex)
                val mimeType = it.getString(mimeIndex) ?: "image/*"
                val dateAdded = it.getLong(dateIndex)
                
                images.add(MediaFileInfo(path, name, size, mimeType, dateAdded))
                count++
            }
        }
        
        return images
    }
    
    /**
     * 비디오 파일을 수집합니다.
     * 최대 50개의 최근 비디오를 날짜 내림차순으로 수집합니다.
     * 
     * @return 비디오 파일 정보 리스트
     */
    private fun collectVideos(): List<MediaFileInfo> {
        val videos = mutableListOf<MediaFileInfo>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA
        )
        
        // ContentResolver는 LIMIT을 지원하지 않으므로 정렬만 하고, 개수는 코드에서 제한
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            
            var count = 0
            val maxCount = 50
            
            while (it.moveToNext() && count < maxCount) {
                val path = it.getString(dataIndex) ?: ""
                val name = it.getString(nameIndex) ?: ""
                val size = it.getLong(sizeIndex)
                val mimeType = it.getString(mimeIndex) ?: "video/*"
                val dateAdded = it.getLong(dateIndex)
                
                videos.add(MediaFileInfo(path, name, size, mimeType, dateAdded))
                count++
            }
        }
        
        return videos
    }
    
    /**
     * 오디오 파일을 수집합니다.
     * 최대 50개의 최근 오디오를 날짜 내림차순으로 수집합니다.
     * 
     * @return 오디오 파일 정보 리스트
     */
    private fun collectAudioFiles(): List<MediaFileInfo> {
        val audioFiles = mutableListOf<MediaFileInfo>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )
        
        // ContentResolver는 LIMIT을 지원하지 않으므로 정렬만 하고, 개수는 코드에서 제한
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            
            var count = 0
            val maxCount = 50
            
            while (it.moveToNext() && count < maxCount) {
                val path = it.getString(dataIndex) ?: ""
                val name = it.getString(nameIndex) ?: ""
                val size = it.getLong(sizeIndex)
                val mimeType = it.getString(mimeIndex) ?: "audio/*"
                val dateAdded = it.getLong(dateIndex)
                
                audioFiles.add(MediaFileInfo(path, name, size, mimeType, dateAdded))
                count++
            }
        }
        
        return audioFiles
    }
}

