#include "Transformer.hpp"
#include <cstring>
#include <thread>
#include "../Console.hpp"

Transformer::Transformer(JNI& jni, Java::ClassLoader& minecraftClassLoader) :
	jni(jni),
    minecraftClassLoader(minecraftClassLoader)
{
    jvmtiCapabilities cap;
    memset(&cap, 0, sizeof(cap));
    cap.can_retransform_classes = JVMTI_ENABLE;
    if (jni.get_jvmti_env()->AddCapabilities(&cap) != JVMTI_ERROR_NONE)
    {
        Console::log_error("Retransform classes not supported");
        return;
    }

    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = ClassFileLoadHook_callback;
    if (jni.get_jvmti_env()->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks)) != JVMTI_ERROR_NONE)
    {
        Console::log_error("Failed to set event callback");
        return;
    }

    if (jni.get_jvmti_env()->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr) != JVMTI_ERROR_NONE)
    {
        Console::log_error("Failed enable event");
        return;
    }

	_is_error = false;
}

Transformer::~Transformer()
{
    jni.get_jvmti_env()->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr);
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    jni.get_jvmti_env()->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks));

    retransform();

    jvmtiCapabilities cap;
    memset(&cap, 0, sizeof(cap));
    cap.can_retransform_classes = JVMTI_ENABLE;
    jni.get_jvmti_env()->RelinquishCapabilities(&cap);

    
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
}

bool Transformer::retransform()
{
    LocalFrame frame(jni);

    constexpr int to_transform_count = sizeof(to_transform) / sizeof(TransformData);
    for (int i = 0; i < to_transform_count; ++i)
    {
        jclass clazz = minecraftClassLoader.findClass(std::string(to_transform[i].target_class));
        if (!clazz) {
            Console::log_error("Could not find class for retransform: " + std::string(to_transform[i].target_class));
            continue;
        }

        jvmtiError status = jni.get_jvmti_env()->RetransformClasses(1, &clazz);

        if (status != JVMTI_ERROR_NONE)
        {
            char* error_name = nullptr;
            jni.get_jvmti_env()->GetErrorName(status, &error_name);
            std::string msg = "Failed to retransform " + std::string(to_transform[i].target_class) + ": ";
            msg += (error_name ? error_name : "unknown error");
            Console::log_error(msg);
            if (error_name) jni.get_jvmti_env()->Deallocate((unsigned char*)error_name);
        }
        else
        {
            Console::log_success("Successfully retransformed: " + std::string(to_transform[i].target_class));
        }
        jni.get_env()->DeleteLocalRef(clazz);
    }

    return true;
}

void Transformer::ClassFileLoadHook_callback(jvmtiEnv* jvmti_env, JNIEnv* jni_env, jclass class_being_redefined, jobject loader, const char* name, jobject protection_domain, jint class_data_len, const unsigned char* class_data, jint* new_class_data_len, unsigned char** new_class_data)
{
    if (!class_being_redefined || disable) return;

    for (const TransformData& transform : to_transform)
    {
        if (transform.target_class != std::string_view(name)) continue;

        JNI jni(jni_env, jvmti_env);
        LocalFrame frame(jni);

        jbyteArray original_class_bytes = jni_env->NewByteArray(class_data_len);
        jni_env->SetByteArrayRegion(original_class_bytes, 0, class_data_len, (jbyte*)class_data);
        jclass Patcher = jni.find_class_any_cl("io/github/lefraudeur/Patcher");
        if (!Patcher) return;
        jmethodID patcher_method = jni_env->GetStaticMethodID(Patcher, transform.patcher_name.data(), "([B)[B");
        if (!patcher_method) return;
        jbyteArray new_class_bytes = (jbyteArray)jni_env->CallStaticObjectMethod(Patcher, patcher_method, original_class_bytes);
        if (!new_class_bytes) return;

        jsize new_size = jni_env->GetArrayLength(new_class_bytes);
        if (!new_size) return;

        unsigned char* new_byte_buff = nullptr;
        jvmti_env->Allocate(new_size, &new_byte_buff);

        jni_env->GetByteArrayRegion(new_class_bytes, 0, new_size, (jbyte*)new_byte_buff);

        
        *new_class_data_len = new_size;
        *new_class_data = new_byte_buff;

        break;
    }
    return;
}
