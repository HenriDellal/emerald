#!/bin/sh
#
#
# Build file for emerald-launcher on jenkins server
#
# Inspired by http://www.hanshq.net/command-line-android.html
#
#

SOURCE=ru/henridellal/emerald
BASE=/opt/
SDK=${BASE}/android-sdk-linux
BUILD_TOOLS=${SDK}/build-tools/27.0.3
PLATFORM=${SDK}/platforms/android-26

# create necessary directories
mkdir -p build/apk build/gen build/obj

# aapt, create R.java
"${BUILD_TOOLS}/aapt" package -f -m -J build/gen/ -S res -M AndroidManifest.xml -I "${PLATFORM}/android.jar"

# javac
javac -source 1.8 -target 1.8 -bootclasspath "${JAVA_HOME}/jre/lib/rt.jar" -classpath "${PLATFORM}/android.jar" -d build/obj build/gen/${SOURCE}/R.java src/com/commonsware/cwac/colormixer/ColorMixer.java src/ru/henridellal/emerald/*.java

# dex
"${BUILD_TOOLS}/dx" --dex --output=build/apk/classes.dex build/obj/

# aapt, create first apk
"${BUILD_TOOLS}/aapt" package -f -M AndroidManifest.xml -S res/  -I "${PLATFORM}/android.jar" -F build/emerald.unsigned.unaligned.apk build/apk/

# zipalign the apk
"${BUILD_TOOLS}/zipalign" -f 4 build/emerald.unsigned.unaligned.apk build/emerald.unsigned.aligned.apk
