#!/data/data/com.termux/files/usr/bin/sh -e 
#
# Copyright 2017 (c) all rights reserved 
# by S D Rausty https://sdrausty.github.io
# Adapted from Adi Lima https://github.com/fx-adi-lima/android-tutorials
#####################################################################
PROJECT_DIR=$HOME/emerald
cd $PROJECT_DIR/app/src/main
if [ ! -d "./bin" ]; then
  # Control will enter here if $DIRECTORY doesn't exist.
mkdir ./bin
fi
if [ ! -d "./assets" ]; then
mkdir ./assets
fi
# create R.java
echo "aapt begun"
aapt package -v -f \
             -M ./AndroidManifest.xml \
             -I $PREFIX/share/java/android.jar \
             -J java \
             -S res \
             -m


# compile the java sources
# THIS EXAMPLE USING ecj, and we should find out which version
# If using jack then we must do like this:
#   jack --classpath $ANDROID_HOME/platforms/android-n/android.jar \
#        --import [path/to/import/lib/*.jar \
#        --output-dex bin/ \
#        src/ gen/
# And then, no more using dx to produce classes.dex

#####################################################################
#
echo "ecj begun"
ecj -d ./obj -classpath $HOME/../usr/share/java/android.jar \
	     -sourcepath ./java $(find java -type f -name "*.java")
#
echo "dx begun"
dx --dex --verbose --output=./bin/classes.dex ./obj

#jack --classpath $PREFIX/share/java/android.jar \
#	--output-dex bin/ \
#	src/ gen/

# make the apk
echo "make the apk"

aapt package -v -f \
             -M ./AndroidManifest.xml \
             -S ./res \
	     -A ./assets \
             -F bin/emerald.apk


# add the classes.dex to the apk
echo "add the classes.dex to the apk"
cd bin
aapt add -f emerald.apk classes.dex

echo "sign the apk"
apksigner $PROJECT_DIR/test.keystore emerald.apk $PROJECT_DIR/emerald.apk

echo "and make it accessible to the outside world"
cd $PROJECT_DIR
chmod 744 emerald.apk

cp emerald.apk /sdcard/Download/
echo "emerald.apk copied to /sdcard/Download/"
