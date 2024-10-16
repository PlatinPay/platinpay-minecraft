import base64
import json
import time
import requests
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey

PRIVATE_KEY_BYTES = base64.b64decode(
    "4AT/FVnvH/D6lLXrSXunQMicAQ1VbZmvZrsfjPsbJx4=" # Testing key, can be pushed to version control. (Base64)
    # Testing public key: MCowBQYDK2VwAyEAAwxHoVBftgojyS3FBDQozjNAIw1vlR/3pH6+JInJtkY= (Base64 X.509)
private_key = Ed25519PrivateKey.from_private_bytes(PRIVATE_KEY_BYTES)

url = "http://localhost:8081"

def sign_data(data):
    data_string = json.dumps(data, separators=(',', ':')).encode('utf-8')
    signature = private_key.sign(data_string)
    return base64.b64encode(signature).decode('utf-8')

def send_request(data):
    payload = {
        "data": data,
        "signature": sign_data(data)
    }

    response = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
    return response

def test_blocked_command():
    data = {
        "playeruuid": "a061e888-bbe1-41cc-8630-f34f6c7b7762",
        "commands": ["stop"],
        "timestamp": int(time.time())
    }
    response = send_request(data)
    print("Blocked Command Test:")
    print(f"Status: {response.status_code}, Response: {response.text}\n")

def test_invalid_request():
    data = {
        "playeruuid": "a061e888-bbe1-41cc-8630-f34f6c7b7762",
        "timestamp": int(time.time())
    }
    response = send_request(data)
    print("Invalid Request Test:")
    print(f"Status: {response.status_code}, Response: {response.text}\n")

def test_replay_attack():
    old_timestamp = int(time.time()) - 100
    data = {
        "playeruuid": "a061e888-bbe1-41cc-8630-f34f6c7b7762",
        "commands": ["say Hello, {playeruuid}!"],
        "timestamp": old_timestamp
    }
    response = send_request(data)
    print("Replay Attack Test:")
    print(f"Status: {response.status_code}, Response: {response.text}\n")

def test_valid_request():
    data = {
        "playeruuid": "a061e888-bbe1-41cc-8630-f34f6c7b7762",
        "commands": ["say Hello, {playeruuid}!", "give {playeruuid} minecraft:diamond 1"],
        "timestamp": int(time.time())
    }
    response = send_request(data)
    print("Valid Request Test:")
    print(f"Status: {response.status_code}, Response: {response.text}\n")

if __name__ == "__main__":
    test_blocked_command()
    test_invalid_request()
    test_replay_attack()
    test_valid_request()