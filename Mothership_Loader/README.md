# Mothership Manager

Android system application for managing public kiosk devices with root access.

## Features

- **Boot Check**: Automatically verifies presence of `com.android.mothershipmanager` package on boot
- **Auto-Download**: Downloads and installs Mothership Manager from remote server if missing
- **Periodic Reboot**: Reboots device every 30 minutes
- **User Warning**: Shows 15-minute countdown warning before reboot

## Requirements

- Android 7.0 (API 24) or higher
- Root access on device
- Device configured as system app

## Installation

1. Build the APK using Android Studio or Gradle
2. Push to device as system app:
   ```bash
   adb root
   adb remount
   adb push app/build/outputs/apk/release/app-release.apk /system/priv-app/MothershipManager/
   adb shell chmod 644 /system/priv-app/MothershipManager/app-release.apk
   adb reboot
   ```

## Configuration

Update the following constants in `MothershipManagerService.java`:

- `DOWNLOAD_URL`: Your server URL hosting the mothership-manager.apk
- `TARGET_PACKAGE`: Package name to check (default: com.android.mothershipmanager)
- `REBOOT_INTERVAL`: Time between reboots (default: 30 minutes)
- `WARNING_TIME`: Warning display time (default: 15 minutes)

## Permissions

The app requires these permissions (granted automatically as system app):
- INTERNET - Download management package
- RECEIVE_BOOT_COMPLETED - Start on device boot
- REQUEST_INSTALL_PACKAGES - Install downloaded APK
- REBOOT - Restart device
- SYSTEM_ALERT_WINDOW - Show warning overlay
- FOREGROUND_SERVICE - Run persistent service

## Security Notes

- This app requires root access and should only be deployed on controlled kiosk devices
- Ensure DOWNLOAD_URL uses HTTPS with proper certificate validation in production
- Consider adding authentication/authorization for APK downloads
- The app runs as a foreground service to prevent termination
