import requests

# URL of the webhook server
url = "http://localhost:8081"

# Data to be sent to the webhook
data = {
    "postData": "Hello from Python test script!"
}

# Send POST request
try:
    response = requests.post(url, data=data)

    # Print the response from the server
    print("Response status code:", response.status_code)
    print("Response body:", response.text)

except requests.exceptions.RequestException as e:
    print(f"Error sending POST request: {e}")