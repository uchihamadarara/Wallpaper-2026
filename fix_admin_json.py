import json

with open('app/google-services.json', 'r') as f:
    data = json.load(f)

# Copy the first client and change package name
client = data['client'][0]
client['client_info']['android_client_info']['package_name'] = 'com.nova.admin'

data['client'] = [client]

with open('admin/google-services.json', 'w') as f:
    json.dump(data, f, indent=2)
