from SqlModule import SqlModule

def main():
    print("\n--- 26AI 모의해킹 SQL Injection---\n")
    sql_module = None
    token_value = ""
    print("\n--- 설정 ---")
    url_input = input("취약한 URL을 입력하세요: ").strip()
    param_input = input("취약한 파라미터명을 입력하세요: ").strip()
    token_value = input("인증 토큰 값을 입력하세요: ").strip()
    sql_module = SqlModule(url_input, param_input, token_value)
    print("\n설정이 완료되었습니다.")

    while True:
        print("\n--- 메뉴 ---")
        print("1: DB 종류 파악")
        print("2: DB 이름 추출")
        print("3: 테이블 이름 추출")
        print("4: 컬럼 이름 추출")
        print("5: 데이터 추출")
        print("exit: 프로그램 종료")
        print("------------")

        user_input = input("선택: ").strip().lower()
        if user_input == "exit":
            print("\n프로그램을 종료합니다.")
            break 
        case_map = {
                    "1": sql_module.getDBType,
                    "2": sql_module.getDBNames,
                    "3": sql_module.getTableNames,
                    "4": sql_module.getColumnNames,
                    "5": sql_module.getData,
                }
        if user_input in case_map:
            if sql_module:
                case_map[user_input]() # 선택된 함수 호출
            else:
                print("URL과 파라미터 설정이 필요합니다.")
        else:
            print(f"\n '{user_input}'은(는) 유효하지 않은 입력입니다. 다시 시도해 주세요.")


if __name__ == "__main__":
    main()