import re
with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    text = f.read()

# find SettingsSection("SUPPORT")
support = '''                SettingsSection("SUPPORT") {
                    SettingsArrowItem(Icons.Outlined.Shield, "Privacy Policy", "Read our privacy policy")
                    SettingsArrowItem(Icons.Outlined.Description, "Terms of Service", "Read our terms of service")
                }
            }'''

text = re.sub(r'                SettingsSection\("SUPPORT"\) \{.*?\n            \}', support, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(text)
