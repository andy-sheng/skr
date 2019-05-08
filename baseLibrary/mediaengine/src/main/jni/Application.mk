APP_ABI 	 := armeabi-v7a arm64-v8a x86
APP_STL 	 := gnustl_static

#APP_CFLAGS  += -g -gdwarf-2
APP_CFLAGS   += -O3
APP_OPTIM 	 := release

APP_PLATFORM := android-14
NDK_TOOLCHAIN_VERSION := 4.9