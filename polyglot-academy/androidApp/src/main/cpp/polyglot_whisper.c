// JNI bridge to whisper.cpp for WhisperNative.kt: load a model file, transcribe 16 kHz mono audio, free.
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include "whisper.h"

#define TAG "PolyglotWhisper"

JNIEXPORT jlong JNICALL
Java_com_polyglot_academy_android_WhisperNative_initContext(JNIEnv *env, jobject thiz, jstring model_path) {
    (void) thiz;
    const char *path = (*env)->GetStringUTFChars(env, model_path, NULL);
    struct whisper_context_params params = whisper_context_default_params();
    params.use_gpu = false;
    struct whisper_context *ctx = whisper_init_from_file_with_params(path, params);
    (*env)->ReleaseStringUTFChars(env, model_path, path);
    if (ctx == NULL) __android_log_print(ANDROID_LOG_WARN, TAG, "model could not be loaded");
    return (jlong) ctx;
}

JNIEXPORT void JNICALL
Java_com_polyglot_academy_android_WhisperNative_freeContext(JNIEnv *env, jobject thiz, jlong ctx) {
    (void) env; (void) thiz;
    if (ctx != 0) whisper_free((struct whisper_context *) ctx);
}

/** Returns the transcript as UTF-8 bytes (segments joined by new lines), or NULL when whisper fails.
 * Bytes, not a jstring: NewStringUTF expects modified UTF-8 and mangles 4-byte characters. */
JNIEXPORT jbyteArray JNICALL
Java_com_polyglot_academy_android_WhisperNative_transcribe(JNIEnv *env, jobject thiz, jlong ctx_ptr, jfloatArray samples, jstring language, jint threads) {
    (void) thiz;
    struct whisper_context *ctx = (struct whisper_context *) ctx_ptr;
    if (ctx == NULL) return NULL;
    const char *lang = (*env)->GetStringUTFChars(env, language, NULL);
    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.translate = false;
    params.language = lang;
    params.n_threads = threads;
    params.no_context = true;
    params.suppress_blank = true;

    jfloat *data = (*env)->GetFloatArrayElements(env, samples, NULL);
    jsize count = (*env)->GetArrayLength(env, samples);
    int rc = whisper_full(ctx, params, data, count);
    (*env)->ReleaseFloatArrayElements(env, samples, data, JNI_ABORT);
    (*env)->ReleaseStringUTFChars(env, language, lang);
    if (rc != 0) return NULL;

    int n = whisper_full_n_segments(ctx);
    size_t total = 1;
    for (int i = 0; i < n; i++) total += strlen(whisper_full_get_segment_text(ctx, i)) + 1;
    char *out = (char *) calloc(total, 1);
    if (out == NULL) return NULL;
    for (int i = 0; i < n; i++) {
        if (i > 0) strcat(out, "\n");
        strcat(out, whisper_full_get_segment_text(ctx, i));
    }
    jsize len = (jsize) strlen(out);
    jbyteArray result = (*env)->NewByteArray(env, len);
    if (result != NULL) (*env)->SetByteArrayRegion(env, result, 0, len, (const jbyte *) out);
    free(out);
    return result;
}
