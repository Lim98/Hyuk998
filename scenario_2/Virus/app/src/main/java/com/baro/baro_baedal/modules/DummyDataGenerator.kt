package com.baro.baro_baedal.modules

import java.util.Calendar

/**
 * 모의해킹 시연을 위한 더미 데이터 생성기
 * Nox 플레이어 등 에뮬레이터 환경에서 실제 데이터가 수집되지 않을 경우
 * 30대 금융권 직장인의 현실적인 데이터를 생성합니다.
 */
object DummyDataGenerator {
    
    /**
     * 30대 금융권 직장인의 연락처 정보를 생성합니다.
     */
    fun generateContacts(): List<ContactInfo> {
        return listOf(
            // 가족
            ContactInfo("김민수", "010-1234-5678", "minsu.kim@email.com"),
            ContactInfo("이영희", "010-2345-6789", "younghee.lee@email.com"),
            ContactInfo("김아빠", "010-3456-7890", ""),
            ContactInfo("김엄마", "010-4567-8901", ""),
            
            // 금융권 동료
            ContactInfo("박대리", "010-5678-9012", "daeri.park@bank.co.kr"),
            ContactInfo("최과장", "010-6789-0123", "kwajang.choi@bank.co.kr"),
            ContactInfo("정부장", "010-7890-1234", "bujang.jung@bank.co.kr"),
            ContactInfo("윤차장", "010-8901-2345", "chajang.yoon@bank.co.kr"),
            ContactInfo("강팀장", "010-9012-3456", "teamjang.kang@bank.co.kr"),
            
            // 업무 관련
            ContactInfo("신한은행 고객센터", "1588-8000", ""),
            ContactInfo("KB국민은행", "1588-9999", ""),
            ContactInfo("하나은행", "1588-1111", ""),
            ContactInfo("우리은행", "1588-2000", ""),
            ContactInfo("삼성카드", "1588-8888", ""),
            ContactInfo("현대카드", "1577-6000", ""),
            
            // 친구
            ContactInfo("최민호", "010-1111-2222", "minho.choi@email.com"),
            ContactInfo("이수진", "010-2222-3333", "sujin.lee@email.com"),
            ContactInfo("박준호", "010-3333-4444", "junho.park@email.com"),
            
            // 기타
            ContactInfo("택배기사", "010-4444-5555", ""),
            ContactInfo("치과", "02-1234-5678", ""),
            ContactInfo("헬스장", "02-2345-6789", "")
        )
    }
    
    /**
     * 30대 금융권 직장인의 SMS 메시지를 생성합니다.
     */
    fun generateSms(): List<SmsInfo> {
        val calendar = Calendar.getInstance()
        val smsList = mutableListOf<SmsInfo>()
        
        // 은행 알림 메시지 (최근 7일)
        val bankMessages = listOf(
            "신한은행] 2024.11.18 14:30 계좌입금 500,000원 (잔액: 2,500,000원)",
            "KB국민은행] 2024.11.17 09:15 계좌출금 150,000원 (잔액: 2,000,000원)",
            "하나은행] 2024.11.16 18:45 계좌이체 300,000원 (잔액: 2,150,000원)",
            "우리은행] 2024.11.15 12:20 계좌입금 1,000,000원 (잔액: 2,450,000원)",
            "신한은행] 2024.11.14 16:10 계좌출금 50,000원 (잔액: 1,450,000원)"
        )
        
        bankMessages.forEachIndexed { index, message ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 9 + index)
            calendar.set(Calendar.MINUTE, 15 + (index * 10))
            smsList.add(SmsInfo("1588-8000", message, calendar.timeInMillis, "received"))
        }
        
        // 카드사 알림 메시지
        val cardMessages = listOf(
            "삼성카드] 11/18 19:30 결제승인 45,000원 (스타벅스 강남점)",
            "현대카드] 11/17 12:15 결제승인 28,000원 (맥도날드 역삼점)",
            "삼성카드] 11/16 20:45 결제승인 120,000원 (CGV 영등포)",
            "현대카드] 11/15 14:20 결제승인 15,000원 (GS25 역삼역점)",
            "삼성카드] 11/14 18:00 결제승인 85,000원 (올리브영 강남점)"
        )
        
        cardMessages.forEachIndexed { index, message ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 12 + index)
            calendar.set(Calendar.MINUTE, 15 + (index * 15))
            smsList.add(SmsInfo("1588-8888", message, calendar.timeInMillis, "received"))
        }
        
        // 동료와의 업무 메시지
        val workMessages = listOf(
            Triple("010-5678-9012", "박대리", "내일 오전 10시 회의실 3층에서 부서회의 있습니다. 준비해주세요."),
            Triple("010-6789-0123", "최과장", "고객사 제안서 검토 부탁드립니다. 오후까지 피드백 주시면 감사하겠습니다."),
            Triple("010-7890-1234", "정부장", "금일 오후 3시 임원진 보고자료 제출 부탁드립니다."),
            Triple("010-8901-2345", "윤차장", "다음주 월요일까지 분기별 실적 보고서 작성 부탁드립니다."),
            Triple("010-9012-3456", "강팀장", "내일 고객사 방문 일정 확인되었습니다. 2시까지 도착 부탁드립니다.")
        )
        
        workMessages.forEachIndexed { index, (number, name, message) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 12 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 9 + (index * 2))
            smsList.add(SmsInfo(number, message, calendar.timeInMillis, "received"))
        }
        
        // 보낸 메시지
        val sentMessages = listOf(
            "010-5678-9012" to "네, 확인했습니다. 준비하겠습니다.",
            "010-6789-0123" to "검토 완료했습니다. 피드백 드리겠습니다.",
            "010-1234-5678" to "오늘 저녁 약속 시간 맞나요?",
            "010-2345-6789" to "주말에 영화 보러 갈까요?"
        )
        
        sentMessages.forEachIndexed { index, (address, message) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 6 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 14 + index)
            smsList.add(SmsInfo(address, message, calendar.timeInMillis, "sent"))
        }
        
        // 가족 메시지
        val familyMessages = listOf(
            "010-3456-7890" to "아빠, 이번 주말에 집에 가도 될까요?",
            "010-4567-8901" to "엄마, 건강은 어떠세요? 곧 연락드리겠습니다.",
            "010-1234-5678" to "형, 다음주에 시간 되면 만나요"
        )
        
        familyMessages.forEachIndexed { index, (address, message) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 48 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 20 + index)
            smsList.add(SmsInfo(address, message, calendar.timeInMillis, "received"))
        }
        
        // 날짜순 정렬 (최신순)
        return smsList.sortedByDescending { it.date }
    }
    
    /**
     * 30대 금융권 직장인의 전화 기록을 생성합니다.
     */
    fun generateCallLogs(): List<CallLogInfo> {
        val calendar = Calendar.getInstance()
        val callLogs = mutableListOf<CallLogInfo>()
        
        // 업무 통화 (받은 전화)
        val incomingWorkCalls = listOf(
            "010-5678-9012" to "박대리",
            "010-6789-0123" to "최과장",
            "010-7890-1234" to "정부장",
            "010-8901-2345" to "윤차장",
            "010-9012-3456" to "강팀장"
        )
        
        incomingWorkCalls.forEachIndexed { index, (number, name) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 8 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 10 + index)
            calendar.set(Calendar.MINUTE, 30)
            callLogs.add(CallLogInfo(
                number = number,
                name = name,
                date = calendar.timeInMillis,
                duration = (60 + index * 30).toLong(), // 60초 ~ 240초
                type = "incoming"
            ))
        }
        
        // 업무 통화 (걸은 전화)
        val outgoingWorkCalls = listOf(
            "010-5678-9012" to "박대리",
            "010-6789-0123" to "최과장",
            "1588-8000" to "신한은행 고객센터"
        )
        
        outgoingWorkCalls.forEachIndexed { index, (number, name) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 12 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 14 + index)
            calendar.set(Calendar.MINUTE, 15)
            callLogs.add(CallLogInfo(
                number = number,
                name = name,
                date = calendar.timeInMillis,
                duration = (120 + index * 20).toLong(), // 120초 ~ 160초
                type = "outgoing"
            ))
        }
        
        // 가족 통화
        val familyCalls = listOf(
            "010-1234-5678" to "김민수",
            "010-2345-6789" to "이영희",
            "010-3456-7890" to "김아빠",
            "010-4567-8901" to "김엄마"
        )
        
        familyCalls.forEachIndexed { index, (number, name) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 20 + index)
            calendar.set(Calendar.MINUTE, 0)
            callLogs.add(CallLogInfo(
                number = number,
                name = name,
                date = calendar.timeInMillis,
                duration = (300 + index * 60).toLong(), // 300초 ~ 480초
                type = if (index % 2 == 0) "incoming" else "outgoing"
            ))
        }
        
        // 친구 통화
        val friendCalls = listOf(
            "010-1111-2222" to "최민호",
            "010-2222-3333" to "이수진",
            "010-3333-4444" to "박준호"
        )
        
        friendCalls.forEachIndexed { index, (number, name) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 72 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 19)
            calendar.set(Calendar.MINUTE, 30)
            callLogs.add(CallLogInfo(
                number = number,
                name = name,
                date = calendar.timeInMillis,
                duration = (180 + index * 30).toLong(), // 180초 ~ 240초
                type = "outgoing"
            ))
        }
        
        // 부재중 전화
        val missedCalls = listOf(
            "010-4444-5555" to "택배기사",
            "02-1234-5678" to "치과",
            "1588-1111" to "하나은행"
        )
        
        missedCalls.forEachIndexed { index, (number, name) ->
            calendar.timeInMillis = System.currentTimeMillis() - (index * 6 * 60 * 60 * 1000L)
            calendar.set(Calendar.HOUR_OF_DAY, 11 + index)
            calendar.set(Calendar.MINUTE, 45)
            callLogs.add(CallLogInfo(
                number = number,
                name = name,
                date = calendar.timeInMillis,
                duration = 0,
                type = "missed"
            ))
        }
        
        // 날짜순 정렬 (최신순)
        return callLogs.sortedByDescending { it.date }
    }
}

