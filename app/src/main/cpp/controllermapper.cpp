// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("controllermapper");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("controllermapper")
//      }
//    }

#include <jni.h>
#include <string>

//#include "getevent.cpp"

extern "C" JNIEXPORT jstring JNICALL
Java_me_ductran_nativecsample_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}