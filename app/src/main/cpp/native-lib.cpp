#include <jni.h>
#include <string>
#include <toys.h>

extern "C" JNIEXPORT jstring  extern "C" JNICALL
Java_lib_atomofiron_toybox_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */,
        jint dur) {
    std::string hello = "Hello from C++";

    char *ptr_array[1] = { "whoami" };
    lib_main(1, ptr_array);

    return env->NewStringUTF(hello.c_str());
}