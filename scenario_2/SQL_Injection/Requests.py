import requests

class Requests:
    def __init__(self, token):
        self.bearer_token = token
        pass

    def send_get_request(self, url, headers=None):
        print(f"{url}")
        if headers is None:
            headers = {}
        if self.bearer_token:
            headers['Authorization'] = f'Bearer {self.bearer_token}'
        try:
            response = requests.get(url, headers=headers, timeout=10)
            response.raise_for_status() 
            return response
        except requests.exceptions.HTTPError as errh:
            return errh.response