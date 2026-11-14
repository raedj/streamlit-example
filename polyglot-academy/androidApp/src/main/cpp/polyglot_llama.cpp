// JNI bridge to llama.cpp for LlamaNative.kt: load a GGUF chat model, answer a conversation with the
// model's own chat template, free. One conversation at a time per handle; cancel() stops generation.
#include <jni.h>
#include <android/log.h>
#include <atomic>
#include <string>
#include <vector>
#include "llama.h"

#define TAG "PolyglotLlama"

namespace {
struct Session {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    const llama_vocab *vocab = nullptr;
    std::atomic<bool> cancel{false};
};

bool abort_requested(void *data) { return static_cast<Session *>(data)->cancel.load(); }

std::string utf8(JNIEnv *env, jstring s) {
    const char *c = env->GetStringUTFChars(s, nullptr);
    std::string out(c);
    env->ReleaseStringUTFChars(s, c);
    return out;
}

/** UTF-8 bytes from a Java string (GetStringUTFChars gives modified UTF-8, wrong for emoji and rare CJK). */
std::string bytes_of(JNIEnv *env, jbyteArray a) {
    jsize n = env->GetArrayLength(a);
    std::string out(static_cast<size_t>(n), '\0');
    env->GetByteArrayRegion(a, 0, n, reinterpret_cast<jbyte *>(out.data()));
    return out;
}
}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_polyglot_academy_android_LlamaNative_load(JNIEnv *env, jobject, jstring path, jint n_ctx, jint threads) {
    static bool initialised = false;
    if (!initialised) { llama_backend_init(); initialised = true; }
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    std::string p = utf8(env, path);
    llama_model *model = llama_model_load_from_file(p.c_str(), mp);
    if (model == nullptr) { __android_log_print(ANDROID_LOG_WARN, TAG, "model could not be loaded"); return 0; }
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = static_cast<uint32_t>(n_ctx);
    cp.n_batch = static_cast<uint32_t>(n_ctx);
    cp.n_threads = threads;
    cp.n_threads_batch = threads;
    llama_context *ctx = llama_init_from_model(model, cp);
    if (ctx == nullptr) { llama_model_free(model); return 0; }
    auto *s = new Session();
    s->model = model; s->ctx = ctx; s->vocab = llama_model_get_vocab(model);
    llama_set_abort_callback(ctx, abort_requested, s);
    return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
Java_com_polyglot_academy_android_LlamaNative_free(JNIEnv *, jobject, jlong handle) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (s == nullptr) return;
    llama_free(s->ctx);
    llama_model_free(s->model);
    delete s;
}

extern "C" JNIEXPORT void JNICALL
Java_com_polyglot_academy_android_LlamaNative_cancel(JNIEnv *, jobject, jlong handle) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (s != nullptr) s->cancel.store(true);
}

/**
 * [roles] and [contents] (UTF-8 bytes) are the conversation, system message first. Returns the answer as
 * UTF-8 bytes; an empty array when the prompt does not fit the context, null on any other failure.
 */
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_polyglot_academy_android_LlamaNative_chat(JNIEnv *env, jobject, jlong handle, jobjectArray roles, jobjectArray contents,
                                                    jint max_tokens, jfloat temperature) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (s == nullptr) return nullptr;
    s->cancel.store(false);

    jsize n = env->GetArrayLength(roles);
    std::vector<std::string> r(n), c(n);
    std::vector<llama_chat_message> msgs(n);
    for (jsize i = 0; i < n; i++) {
        r[i] = utf8(env, static_cast<jstring>(env->GetObjectArrayElement(roles, i)));
        c[i] = bytes_of(env, static_cast<jbyteArray>(env->GetObjectArrayElement(contents, i)));
    }
    for (jsize i = 0; i < n; i++) msgs[i] = {r[i].c_str(), c[i].c_str()};

    const char *tmpl = llama_model_chat_template(s->model, nullptr);
    std::vector<char> buf(8192);
    int len = llama_chat_apply_template(tmpl, msgs.data(), msgs.size(), true, buf.data(), static_cast<int32_t>(buf.size()));
    if (len > static_cast<int>(buf.size())) {
        buf.resize(static_cast<size_t>(len));
        len = llama_chat_apply_template(tmpl, msgs.data(), msgs.size(), true, buf.data(), static_cast<int32_t>(buf.size()));
    }
    if (len < 0) { __android_log_print(ANDROID_LOG_WARN, TAG, "chat template not supported"); return nullptr; }
    std::string prompt(buf.data(), static_cast<size_t>(len));

    // Each turn starts from an empty cache: the whole conversation is in the prompt.
    llama_memory_clear(llama_get_memory(s->ctx), true);
    int n_prompt = -llama_tokenize(s->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), nullptr, 0, true, true);
    std::vector<llama_token> tokens(static_cast<size_t>(n_prompt));
    if (llama_tokenize(s->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), tokens.data(), n_prompt, true, true) < 0) return nullptr;
    if (n_prompt + max_tokens > static_cast<int>(llama_n_ctx(s->ctx))) return env->NewByteArray(0);

    llama_sampler *smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_penalties(llama_vocab_n_tokens(s->vocab), 64, 1.1f, 0.0f, 0.0f));
    if (temperature <= 0.0f) {
        llama_sampler_chain_add(smpl, llama_sampler_init_greedy());
    } else {
        llama_sampler_chain_add(smpl, llama_sampler_init_top_k(40));
        llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.9f, 1));
        llama_sampler_chain_add(smpl, llama_sampler_init_temp(temperature));
        llama_sampler_chain_add(smpl, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
    }

    std::string out;
    llama_batch batch = llama_batch_get_one(tokens.data(), n_prompt);
    llama_token next = 0;
    bool ok = true;
    for (int i = 0; i < max_tokens; i++) {
        if (llama_decode(s->ctx, batch) != 0) { ok = s->cancel.load(); break; }
        next = llama_sampler_sample(smpl, s->ctx, -1);
        if (llama_vocab_is_eog(s->vocab, next)) break;
        char piece[256];
        int m = llama_token_to_piece(s->vocab, next, piece, sizeof(piece), 0, false);
        if (m > 0) out.append(piece, static_cast<size_t>(m));
        if (s->cancel.load()) break;
        batch = llama_batch_get_one(&next, 1);
    }
    llama_sampler_free(smpl);
    if (!ok) return nullptr;

    jbyteArray result = env->NewByteArray(static_cast<jsize>(out.size()));
    if (result != nullptr) env->SetByteArrayRegion(result, 0, static_cast<jsize>(out.size()), reinterpret_cast<const jbyte *>(out.data()));
    return result;
}
