import requests
import json

url = "http://localhost:8081"

data = {
    "playeruuid": "a061e888-bbe1-41cc-8630-f34f6c7b7762",
    "commands": [
        "say Hello, {playeruuid}!",
        "give {playeruuid} minecraft:dirt 1"
    ]
}

try:
    response = requests.post(url, json=data, headers={"Content-Type": "application/json"})

    print("Response status code:", response.status_code)
    print("Response body:", response.text)

except requests.exceptions.RequestException as e:
    print(f"Error sending POST request: {e}")