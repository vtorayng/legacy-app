#!/bin/bash

CONFIG_FILE="V2rayNG/app/config.txt"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Config file not found at $CONFIG_FILE"
    exit 1
fi

# تبدیل CRLF → LF
sed -i 's/\r$//' "$CONFIG_FILE"

# تابع تمیز کردن مقدار
clean() {
    printf '%s' "$1" | tr -d '\r\n'
}

# خواندن کلیدها به صورت امن (بدون source)
appname="$(clean "$(grep '^appname=' "$CONFIG_FILE" | cut -d '=' -f2-)")"
appid="$(clean "$(grep '^appid=' "$CONFIG_FILE" | cut -d '=' -f2-)")"
appversion="$(clean "$(grep '^appversion=' "$CONFIG_FILE" | cut -d '=' -f2-)")"
appversioncode="$(clean "$(grep '^appversioncode=' "$CONFIG_FILE" | cut -d '=' -f2-)")"
appdomain="$(clean "$(grep '^appdomain=' "$CONFIG_FILE" | cut -d '=' -f2-)")"

echo "--- Applying Configurations ---"
echo "App Name: $appname"
echo "App ID: $appid"
echo "Version: $appversion ($appversioncode)"
echo "Domain: $appdomain"

STRINGS_XML="V2rayNG/app/src/main/res/values/strings.xml"
GRADLE_FILE="V2rayNG/app/build.gradle.kts"
API_SERVICE="V2rayNG/app/src/main/java/com/v2ray/ang/service/ApiService.kt"

sed -i "s|<string name=\"app_name\" translatable=\"false\">.*</string>|<string name=\"app_name\" translatable=\"false\">$appname</string>|g" "$STRINGS_XML"

sed -i "s|applicationId = \".*\"|applicationId = \"$appid\"|g" "$GRADLE_FILE"

sed -i "s|versionName = \".*\"|versionName = \"$appversion\"|g" "$GRADLE_FILE"

sed -i "s|versionCode = [0-9]*|versionCode = $appversioncode|g" "$GRADLE_FILE"

sed -i "s|private const val BASE_URL = \".*\"|private const val BASE_URL = \"$appdomain\"|g" "$API_SERVICE"

echo "--- Configuration Applied Successfully ---"
