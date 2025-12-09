import paho.mqtt.client as mqtt
import json
import time
import threading
import os

# -------------------------------------------------------
# CMD CONSTANTS (프로토콜 정의)
# -------------------------------------------------------
CMD_HEARTBEAT        = 0   # 디바이스 → 서버
CMD_STATUS_REQUEST   = 1   # 서버 → 디바이스
CMD_STATUS_RESPONSE  = 2   # 디바이스 → 서버
CMD_DELIVERY_INFO    = 3   # 서버 → 디바이스
CMD_DELIVERY_ACCEPT  = 4   # 디바이스 → 서버
CMD_DELIVERY_DONE    = 5   # 디바이스 → 서버 (배달 완료)
CMD_SERVER_MESSAGE   = 6   # 서버 → 디바이스 (문구 출력)

# -------------------------------------------------------
# 1) 외부 설정 파일 읽기
# -------------------------------------------------------
def load_config(path="device.config"):
    cfg = {}
    try:
        with open(path, "r") as f:
            for line in f:
                if "=" in line:
                    key, value = line.strip().split("=", 1)
                    cfg[key.strip()] = value.strip()
    except FileNotFoundError:
        print(f"[ERROR] 설정 파일 '{path}' 없음. 기본값 사용.")
    return cfg

# 설정값 로딩
config = load_config()

DEVICE_ID = config.get("DEVICE_ID", "")
broker = config.get("BROKER", "")

# -------------------------------------------------------
# 2) 전역 상태 변수
# -------------------------------------------------------
device_status = "NOTBUSY"
current_delivery_name = None

# -------------------------------------------------------
# 3) 배달 스레드
# -------------------------------------------------------
def delivery_job():
    global device_status

    print("배달 수행중...")
    time.sleep(60)

    device_status = "NOTBUSY"
    print("상태 = NOTBUSY")

    done = {"cmd": 5, "status": "BUSYDONE"}
    client.publish(f"device/{DEVICE_ID}/status", json.dumps(done))

# -------------------------------------------------------
# 4) 메시지 수신 핸들러
# -------------------------------------------------------
def on_message(client, userdata, msg):
    global device_status, current_delivery_name

    data = json.loads(msg.payload.decode())
    cmd = data.get("cmd")
    status = data.get("status")

    # ---------------------------
    # STATUS_REQUEST (cmd=1)
    # ---------------------------
    if cmd == CMD_STATUS_REQUEST:
        resp = {"cmd": CMD_STATUS_RESPONSE, "status": device_status}
        client.publish(f"device/{DEVICE_ID}/status", json.dumps(resp))
        return

    # ---------------------------
    # DELIVERY_INFO (cmd=3)
    # ---------------------------
    if cmd == CMD_DELIVERY_INFO:
        print(f"배달 요청 수신 → {data}")

        if device_status == "BUSY":
            print("BUSY.")
            return

        current_delivery_name = data.get("name", "고객님")
        device_status = "BUSY"

        ack = {"cmd": CMD_DELIVERY_ACCEPT, "status": "accepted"}
        client.publish(f"device/{DEVICE_ID}/status", json.dumps(ack))

        threading.Thread(target=delivery_job).start()
        return

    # ---------------------------
    # DELIVERY COMPLETE MESSAGE (cmd=6)
    # ---------------------------
    if cmd == CMD_SERVER_MESSAGE:
        msg_txt = data.get("message", "")
        print(f"서버 메시지 → {msg_txt}")
        os.system("/bin/led.sh " + msg_txt)

# -------------------------------------------------------
# 5) MQTT 연결 핸들러
# -------------------------------------------------------
def on_connect(client, userdata, flags, rc):
    print("Connected:", rc)
    client.subscribe(f"device/{DEVICE_ID}/cmd")
    client.subscribe(f"device/{DEVICE_ID}/status")

# -------------------------------------------------------
# 6) MQTT 실행
# -------------------------------------------------------
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect(broker, 1883, 60)
client.loop_start()

# -------------------------------------------------------
# 7) Heartbeat Loop
# -------------------------------------------------------
while True:
    beat = {"cmd": CMD_HEARTBEAT, "status": "heartbeat"}
    client.publish(f"device/{DEVICE_ID}/status", json.dumps(beat))
    time.sleep(5)
