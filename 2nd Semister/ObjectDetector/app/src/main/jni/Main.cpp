//
// Created by Ascarre on 28-07-2022.
//

#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_ashope_tech_objectdetector_SplashActivity_GetURL(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("https://assets9.lottiefiles.com/packages/lf20_jpxs88xd.json");
}