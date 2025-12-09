import paho.mqtt.client as mqtt
import json
import time
import threading

broker = "54.180.151.178"

# --------------------------
# 배달 데이터
# --------------------------
delivery_list = [
    {"address": "서울특별시 강남구 테헤란로 123",          "name": "홍길동",   "phone": "010-1111-1111"},
    {"address": "서울특별시 서초구 서초대로 45길 19",      "name": "김철수",   "phone": "010-2222-2222"},
    {"address": "서울특별시 송파구 올림픽로 312",          "name": "박영희",   "phone": "010-3333-3333"},
    {"address": "서울특별시 동작구 상도로 87",             "name": "이순신",   "phone": "010-4444-4444"},
    {"address": "서울특별시 관악구 봉천로 215",            "name": "장보고",   "phone": "010-5555-5555"},
    {"address": "서울특별시 용산구 이태원로 147",          "name": "신사임당", "phone": "010-6666-6666"},

    {"address": "서울특별시 중구 세종대로 110",            "name": "안중근",   "phone": "010-7777-7777"},
    {"address": "서울특별시 종로구 종로 1길 36",           "name": "세종대왕", "phone": "010-8888-8888"},
    {"address": "서울특별시 은평구 통일로 684",            "name": "정약용",   "phone": "010-9999-9999"},
    {"address": "서울특별시 노원구 노해로 502",            "name": "강감찬",   "phone": "010-1212-1212"},

    {"address": "서울특별시 마포구 월드컵북로 400",         "name": "최무선",   "phone": "010-2323-2323"},
    {"address": "서울특별시 마포구 서교동 395-12",         "name": "최무선",   "phone": "010-2323-2323"},  # 중복 그대로 유지

    {"address": "서울특별시 성동구 왕십리로 222",          "name": "김유신",   "phone": "010-3434-3434"},
    {"address": "서울특별시 강북구 도봉로 182",            "name": "유관순",   "phone": "010-4545-4545"},

    {"address": "서울특별시 광진구 능동로 110",            "name": "최재형",   "phone": "010-5656-5656"},
    {"address": "서울특별시 구로구 디지털로 300",          "name": "정몽주",   "phone": "010-6767-6767"},

    {"address": "서울특별시 금천구 시흥대로 153",          "name": "김홍도",   "phone": "010-7878-7878"},
    {"address": "서울특별시 중랑구 망우로 353",            "name": "신윤복",   "phone": "010-8989-8989"},
    {"address": "서울특별시 영등포구 국회대로 760",        "name": "허준",    "phone": "010-9090-9090"},
    {"address": "서울특별시 성북구 보문로 178",            "name": "김시민",   "phone": "010-0101-0101"},

    {"address": "서울특별시 도봉구 도봉로 870",            "name": "강호동",   "phone": "010-0202-0202"},
]

delivery_index = 0
active_devices = set()
DELIVERY_SENT = False
SELECTED_DEVICE = None
last_delivery_name = {}


# ==========================================================
# MQTT EVENT HANDLERS
# ==========================================================

def on_connect(client, userdata, flags, rc):
    print("Connected:", rc)
    client.subscribe("device/+/status")
    client.subscribe("device/+/cmd")


def on_message(client, userdata, msg):
    global DELIVERY_SENT, SELECTED_DEVICE, delivery_index

    topic = msg.topic
    raw = msg.payload.decode()

    try:
        payload = json.loads(raw)
    except:
        print("[ERROR] JSON decode error:", raw)
        return

    parts = topic.split("/")
    device_id = parts[1]
    cmd = payload.get("cmd")

    # HEARTBEAT
    if cmd == 0:
        print(f"[HB] device/{device_id}")
        active_devices.add(device_id)
        return

    # STATUS_RESPONSE
    if cmd == 2:
        status = payload.get("status")
        print(f"[STATUS] device/{device_id} = {status}")

        if status == "NOTBUSY" and not DELIVERY_SENT:
            SELECTED_DEVICE = device_id
            send_delivery_info(device_id)
            DELIVERY_SENT = True
        return

    # DELIVERY_ACCEPT
    if cmd == 4:
        print(f"[ACCEPT] device/{device_id} accepted delivery")
        return

    # BUSYDONE (배달 완료)
    if cmd == 5:
        print(f" device/{device_id} 배달 완료 보고")

        name = last_delivery_name.get(device_id, "고객님")
        msg = {
            "cmd": 6,
            "message": f"배달이 완료되었습니다. {name}님 주문해주셔서 감사합니다."
        }

        client.publish(f"device/{device_id}/cmd", json.dumps(msg))
        print(f"[SEND COMPLETE_MSG] → device/{device_id}")
        return


# ==========================================================
# 배달 정보 전송
# ==========================================================

def send_delivery_info(device_id):
    global delivery_index

    info = delivery_list[delivery_index]
    print(f"[SEND DELIVERY_INFO] #{delivery_index+1} → device/{device_id}")

    last_delivery_name[device_id] = info["name"]

    data = {
        "cmd": 3,
        "address": info["address"],
        "name": info["name"],
        "phone": info["phone"]
    }

    client.publish(f"device/{device_id}/cmd", json.dumps(data))

    delivery_index = (delivery_index + 1) % len(delivery_list)


# ==========================================================
# 5초마다 STATUS_REQUEST 요청
# ==========================================================

def status_request_loop():
    global DELIVERY_SENT
    while True:
        if len(active_devices) > 0:
            print(f"\nSTATUS_REQUEST → active devices: {list(active_devices)}")
            DELIVERY_SENT = False

def status_request_loop():
    global DELIVERY_SENT
    while True:
        if len(active_devices) > 0:
            print(f"\nSTATUS_REQUEST → active devices: {list(active_devices)}")
            DELIVERY_SENT = False

            for dev in active_devices:
                req = {"cmd": 1}
                print(f"[SEND] STATUS_REQUEST → device/{dev}/cmd")
                client.publish(f"device/{dev}/cmd", json.dumps(req))

        time.sleep(5)


# ==========================================================
# MQTT 시작
# ==========================================================

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect(broker, 1883, 60)

threading.Thread(target=status_request_loop, daemon=True).start()

client.loop_forever()
