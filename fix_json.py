import json

with open('app/google-services.json', 'r') as f:
    data = json.load(f)

data['client'][1]['client_info']['android_client_info']['package_name'] = 'com.nova.admin'

with open('app/google-services.json', 'w') as f:
    json.dump(data, f, indent=2)
