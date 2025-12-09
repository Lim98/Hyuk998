package com.baro.baro_baedal.modules

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

/**
 * 문서 파일을 수집하는 클래스
 * 다운로드 폴더와 Documents 폴더에서 문서 파일(PDF, DOC, XLS, XLSX, TXT 등)을 추출합니다.
 * 저장소 접근 권한이 필요합니다.
 */
class DocumentCollector(private val context: Context) {
    
    /**
     * 문서 파일을 수집합니다.
     * 여러 방법을 시도하여 문서 파일을 조회합니다.
     * 
     * @return 문서 파일 정보 리스트
     */
    fun collect(): List<DocumentInfo> {
        val documents = mutableListOf<DocumentInfo>()
        
        Log.d("DocumentCollector", "Starting document collection...")
        
        // Android 15+에서는 문서 파일 접근 권한이 다를 수 있으므로
        // 권한 체크를 완화하고 MediaStore API를 먼저 시도
        // MediaStore API는 일부 경우 권한 없이도 작동할 수 있음
        
        // 여러 방법을 시도
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 11+에서는 MediaStore.Downloads를 사용하면 권한 없이도 다운로드 폴더 접근 가능
            // 하지만 MediaStore가 파일을 인덱싱해야 함
            
            // Android 10+ : MediaStore.Downloads 사용 (다운로드 폴더의 모든 파일)
            // Android 11+에서는 권한 없이도 접근 가능
            Log.d("DocumentCollector", "Trying MediaStore.Downloads (download folder)...")
            collectUsingMediaStoreDownloads(documents)
            
            // MediaStore.Files에서 모든 파일 확인 (백업)
            Log.d("DocumentCollector", "Trying MediaStore.Files (all files)...")
            collectUsingMediaStore(documents)
            
            // MIME 타입으로 직접 검색 (추가 시도)
            Log.d("DocumentCollector", "Trying MIME type search...")
            collectByMimeType(documents)
            
            // MediaStore가 비어있으면 스캔 시도
            if (documents.isEmpty()) {
                Log.d("DocumentCollector", "MediaStore is empty, scanning files to index...")
                val scannedFileUris = scanDownloadFolder()
                
                // 스캔된 URI를 직접 사용하여 문서 추가
                if (scannedFileUris.isNotEmpty()) {
                    Log.d("DocumentCollector", "Using scanned file URIs directly...")
                    addScannedFilesFromUris(scannedFileUris, documents)
                }
            }
        }
        
        // 파일 시스템 직접 접근 시도 (권한 체크 완화)
        // Android 15에서는 권한이 있어도 Scoped Storage 때문에 제한될 수 있음
        Log.d("DocumentCollector", "Trying FileSystem access...")
        collectFromFileSystem(documents)
        
        Log.d("DocumentCollector", "Collected ${documents.size} documents total")
        return documents
    }
    
    /**
     * MediaStore에 다운로드 폴더의 파일들을 강제로 스캔하도록 요청합니다.
     * MediaStore가 파일을 인덱싱하지 않았을 경우를 대비합니다.
     * 
     * @return 스캔된 파일 URI와 경로 매핑 (스캔 완료 후 직접 사용)
     */
    private fun scanDownloadFolder(): Map<String, Uri> {
        val scannedFiles = mutableMapOf<String, Uri>() // 파일 경로 -> URI 매핑
        
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists() && downloadDir.isDirectory) {
                Log.d("DocumentCollector", "Scanning download folder: ${downloadDir.absolutePath}")
                
                val filePaths = mutableListOf<String>()
                
                // MANAGE_EXTERNAL_STORAGE 권한 체크 (Android 11+)
                val hasManageStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    false
                }
                
                if (hasManageStoragePermission) {
                    Log.d("DocumentCollector", "MANAGE_EXTERNAL_STORAGE permission granted, using listFiles()")
                    // 권한이 있으면 listFiles() 사용
                    try {
                        val files = downloadDir.listFiles()
                        if (files != null && files.isNotEmpty()) {
                            files.forEach { file ->
                                if (file.isFile) {
                                    val extension = file.name.substringAfterLast('.', "").lowercase()
                                    if (isSupportedDocumentType(extension)) {
                                        filePaths.add(file.absolutePath)
                                        Log.d("DocumentCollector", "Found file to scan: ${file.absolutePath}")
                                    }
                                }
                            }
                        }
                    } catch (e: SecurityException) {
                        Log.w("DocumentCollector", "SecurityException in listFiles() even with MANAGE_EXTERNAL_STORAGE", e)
                    } catch (e: Exception) {
                        Log.w("DocumentCollector", "listFiles() failed", e)
                    }
                } else {
                    Log.d("DocumentCollector", "MANAGE_EXTERNAL_STORAGE permission not granted")
                    Log.d("DocumentCollector", "Cannot use listFiles() without MANAGE_EXTERNAL_STORAGE permission")
                    Log.d("DocumentCollector", "Will rely on MediaStore queries instead")
                }
                
                if (filePaths.isNotEmpty()) {
                    Log.d("DocumentCollector", "Scanning ${filePaths.size} files in download folder")
                    
                    // 파일 경로 배열 생성
                    val validFilePaths = filePaths.filter { 
                        val file = File(it)
                        file.exists() && file.isFile
                    }.toTypedArray()
                    
                    if (validFilePaths.isNotEmpty()) {
                        // 동기적으로 스캔 완료 대기
                        val scanComplete = java.util.concurrent.CountDownLatch(validFilePaths.size)
                        
                        MediaScannerConnection.scanFile(
                            context,
                            validFilePaths,
                            null
                        ) { path, uri ->
                            scannedFiles[path] = uri
                            Log.d("DocumentCollector", "Scanned: $path -> $uri")
                            scanComplete.countDown()
                        }
                        
                        // 스캔 완료 대기 (최대 5초)
                        try {
                            scanComplete.await(5, java.util.concurrent.TimeUnit.SECONDS)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    } else {
                        Log.w("DocumentCollector", "No valid file paths to scan")
                    }
                } else {
                    Log.d("DocumentCollector", "No files found via listFiles(), folder scan completed")
                }
            }
        } catch (e: SecurityException) {
            Log.e("DocumentCollector", "SecurityException scanning download folder", e)
        } catch (e: Exception) {
            Log.e("DocumentCollector", "Error scanning download folder", e)
        }
        
        return scannedFiles
    }
    
    /**
     * MediaStore.Downloads를 사용하여 다운로드 폴더의 문서 파일을 수집합니다.
     * Android 10 이상에서 사용 가능합니다.
     * 
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun collectUsingMediaStoreDownloads(documents: MutableList<DocumentInfo>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.MIME_TYPE
        )
        
        // Android 10+에서는 DATA 컬럼이 없을 수 있으므로 제외
        // MediaStore.Downloads는 다운로드 폴더의 모든 파일을 반환
        try {
            val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            Log.d("DocumentCollector", "Querying MediaStore.Downloads with URI: $uri")
            
            val cursor: Cursor? = contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Downloads.DATE_ADDED} DESC"
            )
            
            if (cursor == null) {
                Log.w("DocumentCollector", "MediaStore.Downloads cursor is null - may need permission")
                return
            }
            
            cursor.use {
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Downloads.MIME_TYPE)
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                
                var totalCount = 0
                var count = 0
                
                // 커서가 비어있는지 확인
                if (it.count == 0) {
                    Log.w("DocumentCollector", "MediaStore.Downloads returned empty cursor - no files in download folder or permission issue")
                }
                
                while (it.moveToNext()) {
                    totalCount++
                    val name = it.getString(nameIndex) ?: ""
                    val size = it.getLong(sizeIndex)
                    val fileMimeType = it.getString(mimeIndex) ?: ""
                    val id = it.getLong(idIndex)
                    
                    Log.d("DocumentCollector", "Found file in Downloads: $name, mimeType: $fileMimeType")
                    
                    // 파일 확장자 확인
                    val extension = name.substringAfterLast('.', "").lowercase()
                    
                    if (isSupportedDocumentType(extension)) {
                        // URI로 경로 생성
                        val fileUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val path = fileUri.toString()
                        
                        // 중복 체크 (이름으로)
                        if (!documents.any { doc -> doc.name == name }) {
                            documents.add(
                                DocumentInfo(
                                    path = path,
                                    name = name,
                                    size = size,
                                    mimeType = fileMimeType.ifEmpty { MimeTypeHelper.getMimeType(extension) }
                                )
                            )
                            count++
                            Log.d("DocumentCollector", "✓ Added document: $name (${extension})")
                        }
                    } else {
                        Log.d("DocumentCollector", "Skipped file (not supported type): $name (${extension})")
                    }
                }
                Log.d("DocumentCollector", "MediaStore.Downloads: Found $count documents out of $totalCount total files")
            }
        } catch (e: SecurityException) {
            Log.e("DocumentCollector", "SecurityException using MediaStore.Downloads - permission denied", e)
        } catch (e: Exception) {
            Log.e("DocumentCollector", "Error using MediaStore.Downloads", e)
            e.printStackTrace()
        }
    }
    
    /**
     * MediaStore.Files API를 사용하여 다운로드 폴더의 문서 파일을 수집합니다.
     * RELATIVE_PATH를 사용하여 Download 폴더의 파일을 찾습니다.
     * 
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun collectUsingMediaStore(documents: MutableList<DocumentInfo>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        
        // Android 10+에서는 RELATIVE_PATH 사용
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.RELATIVE_PATH
        )
        
        // 필터 없이 모든 파일 가져오기 (나중에 경로로 필터링)
        // Android 15에서는 RELATIVE_PATH 필터가 작동하지 않을 수 있음
        try {
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )
            
            if (cursor == null) {
                Log.w("DocumentCollector", "MediaStore.Files cursor is null")
                return
            }
            
            cursor.use {
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val relativePathIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                
                var totalCount = 0
                var count = 0
                
                val totalCursorCount = it.count
                Log.d("DocumentCollector", "MediaStore.Files cursor count: $totalCursorCount")
                
                if (totalCursorCount == 0) {
                    Log.w("DocumentCollector", "MediaStore.Files returned 0 files - MediaStore may not be indexed yet")
                    // MediaStore가 비어있으면 파일명으로 직접 검색 시도
                    searchByFilename(documents)
                    return@use
                }
                
                var checkedCount = 0
                val maxCheck = 2000 // 최대 2000개 파일 확인 (더 많은 파일 확인)
                
                while (it.moveToNext() && checkedCount < maxCheck) {
                    checkedCount++
                    totalCount++
                    val name = it.getString(nameIndex) ?: ""
                    val size = it.getLong(sizeIndex)
                    val fileMimeType = it.getString(mimeIndex) ?: ""
                    val id = it.getLong(idIndex)
                    val relativePath = it.getString(relativePathIndex) ?: ""
                    
                    // 파일 확장자 확인
                    val extension = name.substringAfterLast('.', "").lowercase()
                    
                    // 다운로드 폴더나 Documents 폴더에 있는 파일인지 확인
                    val isInTargetFolder = relativePath.contains("Download/", ignoreCase = true) ||
                                          relativePath.contains("Documents/", ignoreCase = true) ||
                                          relativePath.startsWith("Download/", ignoreCase = true) ||
                                          relativePath.startsWith("Documents/", ignoreCase = true) ||
                                          name.contains("sample", ignoreCase = true) || // 테스트용: 파일명에 특정 키워드가 있으면 포함
                                          name.contains("예제", ignoreCase = true)
                    
                    if (isInTargetFolder && isSupportedDocumentType(extension)) {
                        // URI로 경로 생성
                        val fileUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )
                        val path = fileUri.toString()
                        
                        // 중복 체크 (이름으로)
                        if (!documents.any { doc -> doc.name == name }) {
                            documents.add(
                                DocumentInfo(
                                    path = path,
                                    name = name,
                                    size = size,
                                    mimeType = fileMimeType.ifEmpty { MimeTypeHelper.getMimeType(extension) }
                                )
                            )
                            count++
                            Log.d("DocumentCollector", "✓ Added document: $name (${extension}) from $relativePath")
                        }
                    }
                    
                    // 처음 10개 파일은 로그로 출력 (디버깅용)
                    if (checkedCount <= 10) {
                        Log.d("DocumentCollector", "File $checkedCount: $name, path: $relativePath, ext: $extension")
                    }
                }
                Log.d("DocumentCollector", "MediaStore.Files: Found $count documents out of $totalCount total files")
            }
        } catch (e: Exception) {
            Log.e("DocumentCollector", "Error using MediaStore.Files", e)
            e.printStackTrace()
        }
    }
    
    /**
     * MIME 타입으로 문서 파일을 검색합니다.
     * 
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun collectByMimeType(documents: MutableList<DocumentInfo>) {
        val contentResolver: ContentResolver = context.contentResolver
        
        // 지원되는 문서 MIME 타입들
        val mimeTypes = listOf(
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )
        
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        
        mimeTypes.forEach { mimeType ->
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf(mimeType)
            
            try {
                val cursor: Cursor? = contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                
                cursor?.use {
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val idIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    
                    var foundCount = 0
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex) ?: ""
                        val size = it.getLong(sizeIndex)
                        val fileMimeType = it.getString(mimeIndex) ?: ""
                        val id = it.getLong(idIndex)
                        
                        val fileUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )
                        val path = fileUri.toString()
                        
                        if (!documents.any { doc -> doc.name == name }) {
                            documents.add(
                                DocumentInfo(
                                    path = path,
                                    name = name,
                                    size = size,
                                    mimeType = fileMimeType
                                )
                            )
                            foundCount++
                            Log.d("DocumentCollector", "✓ Found by MIME type ($mimeType): $name")
                        }
                    }
                    if (foundCount > 0) {
                        Log.d("DocumentCollector", "MIME type search ($mimeType): Found $foundCount files")
                    }
                }
            } catch (e: Exception) {
                Log.e("DocumentCollector", "Error searching by MIME type: $mimeType", e)
            }
        }
    }
    
    /**
     * 스캔된 파일 URI를 직접 사용하여 문서 정보를 수집합니다.
     * MediaStore 조회 없이 스캔된 URI에서 직접 정보를 가져옵니다.
     * 
     * @param scannedFileUris 스캔된 파일 경로와 URI 매핑
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun addScannedFilesFromUris(scannedFileUris: Map<String, Uri>, documents: MutableList<DocumentInfo>) {
        val contentResolver: ContentResolver = context.contentResolver
        
        scannedFileUris.forEach { (filePath, uri) ->
            try {
                val file = File(filePath)
                val fileName = file.name
                val extension = fileName.substringAfterLast('.', "").lowercase()
                
                // 지원되는 문서 형식인지 확인
                if (!isSupportedDocumentType(extension)) {
                    return@forEach
                }
                
                // URI에서 파일 정보 가져오기
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
                
                val cursor: Cursor? = contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                        val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                        
                        val name = it.getString(nameIndex) ?: fileName
                        val size = it.getLong(sizeIndex)
                        val fileMimeType = it.getString(mimeIndex) ?: ""
                        
                        // 중복 체크
                        if (!documents.any { doc -> doc.name == name }) {
                            documents.add(
                                DocumentInfo(
                                    path = uri.toString(),
                                    name = name,
                                    size = size,
                                    mimeType = fileMimeType.ifEmpty { MimeTypeHelper.getMimeType(extension) }
                                )
                            )
                            Log.d("DocumentCollector", "✓ Added scanned file: $name")
                        }
                    } else {
                        // URI에서 직접 정보를 가져올 수 없으면 파일 시스템에서 가져오기
                        try {
                            if (file.exists() && file.isFile) {
                                val name = file.name
                                val size = file.length()
                                
                                if (!documents.any { doc -> doc.name == name }) {
                                    documents.add(
                                        DocumentInfo(
                                            path = uri.toString(),
                                            name = name,
                                            size = size,
                                            mimeType = MimeTypeHelper.getMimeType(extension)
                                        )
                                    )
                                    Log.d("DocumentCollector", "✓ Added scanned file (from file system): $name")
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("DocumentCollector", "Cannot access file: $filePath", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DocumentCollector", "Error processing scanned file URI: $filePath", e)
            }
        }
    }
    
    /**
     * 파일명으로 직접 검색하여 문서 파일을 찾습니다.
     * MediaStore가 비어있을 때 사용하는 백업 방법입니다.
     * 
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun searchByFilename(documents: MutableList<DocumentInfo>) {
        val contentResolver: ContentResolver = context.contentResolver
        
        // 지원되는 문서 확장자들
        val extensions = listOf("pdf", "xlsx", "xls", "doc", "docx", "txt")
        
        extensions.forEach { ext ->
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            
            // 파일명에 확장자가 포함된 파일 검색
            val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%.$ext")
            
            try {
                val cursor: Cursor? = contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                
                cursor?.use {
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val idIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    
                    var foundCount = 0
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex) ?: ""
                        val size = it.getLong(sizeIndex)
                        val fileMimeType = it.getString(mimeIndex) ?: ""
                        val id = it.getLong(idIndex)
                        
                        // 모든 문서 파일을 수집 (필터 제거)
                        val fileUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )
                        val path = fileUri.toString()
                        
                        if (!documents.any { doc -> doc.name == name }) {
                            documents.add(
                                DocumentInfo(
                                    path = path,
                                    name = name,
                                    size = size,
                                    mimeType = fileMimeType.ifEmpty { MimeTypeHelper.getMimeType(ext) }
                                )
                            )
                            foundCount++
                            Log.d("DocumentCollector", "✓ Found by filename search: $name (.$ext)")
                        }
                    }
                    Log.d("DocumentCollector", "Filename search for .$ext: Found $foundCount files")
                }
            } catch (e: Exception) {
                Log.e("DocumentCollector", "Error searching by filename for .$ext", e)
            }
        }
    }
    
    /**
     * 파일 시스템을 직접 접근하여 문서 파일을 수집합니다.
     * 백업 방법으로 사용됩니다.
     * 
     * @param documents 수집된 문서를 추가할 리스트
     */
    private fun collectFromFileSystem(documents: MutableList<DocumentInfo>) {
        // 다운로드 폴더 경로들 시도 (중복 제거)
        val downloadPaths = mutableSetOf<File>().apply {
            add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
            add(File(Environment.getExternalStorageDirectory(), "Download"))
            add(File("/storage/emulated/0/Download"))
            add(File("/sdcard/Download"))
            // Android 15에서 사용 가능한 경로들
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadDir != null) {
                    add(downloadDir)
                }
            }
        }
        
        var foundCount = 0
        downloadPaths.forEach { downloadDir ->
            try {
                val absolutePath = downloadDir.absolutePath
                Log.d("DocumentCollector", "Checking download path: $absolutePath")
                
                if (!downloadDir.exists()) {
                    Log.d("DocumentCollector", "Path does not exist: $absolutePath")
                    return@forEach
                }
                
                if (!downloadDir.isDirectory) {
                    Log.d("DocumentCollector", "Path is not a directory: $absolutePath")
                    return@forEach
                }
                
                if (!downloadDir.canRead()) {
                    Log.w("DocumentCollector", "Cannot read directory: $absolutePath")
                    return@forEach
                }
                
                val files = downloadDir.listFiles()
                if (files == null) {
                    Log.w("DocumentCollector", "listFiles() returned null for: $absolutePath")
                    return@forEach
                }
                
                Log.d("DocumentCollector", "Found ${files.size} files in download directory: $absolutePath")
                
                files.forEach { file ->
                    try {
                        val fileName = file.name
                        val isFile = file.isFile
                        val canRead = file.canRead()
                        val extension = if (fileName.contains('.')) {
                            fileName.substringAfterLast('.', "").lowercase()
                        } else {
                            ""
                        }
                        
                        Log.d("DocumentCollector", "Checking file: $fileName, extension: $extension, isFile: $isFile, canRead: $canRead")
                        
                        if (isFile && canRead) {
                            val isSupported = isSupportedDocumentType(extension)
                            Log.d("DocumentCollector", "File $fileName: extension=$extension, supported=$isSupported")
                            
                            if (isSupported) {
                                // 중복 체크
                                val filePath = file.absolutePath
                                if (!documents.any { doc -> doc.path == filePath }) {
                                    val fileSize = try {
                                        file.length()
                                    } catch (e: Exception) {
                                        Log.w("DocumentCollector", "Cannot get file size for: $fileName", e)
                                        0L
                                    }
                                    
                                    documents.add(
                                        DocumentInfo(
                                            path = filePath,
                                            name = fileName,
                                            size = fileSize,
                                            mimeType = MimeTypeHelper.getMimeType(extension)
                                        )
                                    )
                                    foundCount++
                                    Log.d("DocumentCollector", "✓ Added document: $fileName")
                                }
                            } else {
                                Log.d("DocumentCollector", "File $fileName is not a supported document type")
                            }
                        } else {
                            Log.d("DocumentCollector", "Skipping $fileName: isFile=$isFile, canRead=$canRead")
                        }
                    } catch (e: Exception) {
                        Log.e("DocumentCollector", "Error processing file: ${file.name}", e)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("DocumentCollector", "SecurityException accessing path: ${downloadDir.absolutePath}", e)
            } catch (e: Exception) {
                Log.e("DocumentCollector", "Error accessing path: ${downloadDir.absolutePath}", e)
                e.printStackTrace()
            }
        }
        
        // Documents 폴더 경로들 시도
        val documentsPaths = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            File(Environment.getExternalStorageDirectory(), "Documents"),
            File("/storage/emulated/0/Documents"),
            File("/sdcard/Documents")
        )
        
        documentsPaths.forEach { documentsDir ->
            try {
                if (documentsDir.exists() && documentsDir.isDirectory) {
                    Log.d("DocumentCollector", "Checking documents path: ${documentsDir.absolutePath}")
                    val files = documentsDir.listFiles()
                    Log.d("DocumentCollector", "Found ${files?.size ?: 0} files in documents directory")
                    
                    files?.forEach { file ->
                        Log.d("DocumentCollector", "Checking file: ${file.name}, extension: ${file.extension}, isFile: ${file.isFile}")
                        
                        if (file.isFile) {
                            val extension = file.extension.lowercase()
                            val isSupported = isSupportedDocumentType(extension)
                            Log.d("DocumentCollector", "File ${file.name}: extension=$extension, supported=$isSupported")
                            
                            if (isSupported) {
                                // 중복 체크
                                if (!documents.any { doc -> doc.path == file.absolutePath }) {
                                    documents.add(
                                        DocumentInfo(
                                            path = file.absolutePath,
                                            name = file.name,
                                            size = file.length(),
                                            mimeType = MimeTypeHelper.getMimeType(extension)
                                        )
                                    )
                                    foundCount++
                                    Log.d("DocumentCollector", "✓ Added document: ${file.name}")
                                }
                            }
                        }
                    }
                } else {
                    Log.d("DocumentCollector", "Path does not exist or is not a directory: ${documentsDir.absolutePath}, exists: ${documentsDir.exists()}")
                }
            } catch (e: Exception) {
                Log.e("DocumentCollector", "Error accessing path: ${documentsDir.absolutePath}", e)
                e.printStackTrace()
            }
        }
        
        Log.d("DocumentCollector", "FileSystem: Found $foundCount documents")
    }
    
    /**
     * 파일 확장자가 지원되는 문서 형식인지 확인합니다.
     * 
     * @param extension 파일 확장자 (예: "pdf", "doc")
     * @return 지원되는 형식이면 true
     */
    private fun isSupportedDocumentType(extension: String): Boolean {
        return extension.equals("pdf", true) ||
                extension.equals("doc", true) ||
                extension.equals("docx", true) ||
                extension.equals("xls", true) ||
                extension.equals("xlsx", true) ||
                extension.equals("txt", true)
    }
}

/**
 * MIME 타입을 반환하는 유틸리티 객체
 */
object MimeTypeHelper {
    /**
     * 파일 확장자에 해당하는 MIME 타입을 반환합니다.
     * 
     * @param extension 파일 확장자 (예: "pdf")
     * @return MIME 타입 (예: "application/pdf")
     */
    fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}

