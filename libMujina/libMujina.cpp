#ifdef _WIN32
    #include <Windows.h>
#elif __linux__
    #include <X11/Xlib.h>
    #include <X11/Xutil.h>
#endif

#include <thread>
#include <iostream>
#include <sstream>
#include <string>
#include <JNI/jni.h>
#include "Console.hpp"
#include "JarLoader/JarLoader.hpp"
#include "JNI/JNI.hpp"
#include "InjectableJar/InjectableJar.jar.hpp"
#include "Java/Java.hpp"
#include "ClassFileParser/ClassFileParser.hpp"
#include "Transformer/Transformer.hpp"
#include "mappings.hpp"
#include "StringCleaner/StringCleaner.hpp"

#ifdef __linux__
static Display* display = nullptr;
#endif

static bool is_uninject_key_pressed()
{
#ifdef _WIN32
    return GetAsyncKeyState(VK_END);
#elif __linux__
    static KeyCode keycode = XKeysymToKeycode(display, XK_End);

    char key_states[32] = { '\0'};
    XQueryKeymap(display, key_states);

    return ( key_states[keycode << 3] & ( 1 << (keycode & 7) ) );
#endif
}

static void jni_related(void* modaddr)
{
    JNI jni{};
    if (!jni) return;
    JNIEnv* env = jni.get_env();

    {
        LocalFrame frame(jni);

        Java::ClassLoader minecraftClassLoader = Java::ClassLoader(jni.get_class_loader(std::string(Mappings::net_minecraft_client_MinecraftClient)), jni);
        std::ostringstream msg{};
        Console::log_success((std::ostringstream() << "minecraft ClassLoader: " << (void*)minecraftClassLoader).str());


        Java::URLClassLoader mujinaClassLoader = Java::URLClassLoader::new_object(jni, "file:///C:/Windows/win.ini", minecraftClassLoader);
        Console::log_success((std::ostringstream() << "mujina ClassLoader: " << (void*)mujinaClassLoader).str());

        JarLoader jarLoader{ jni, mujinaClassLoader, minecraftClassLoader };
        if (!jarLoader) return;

        {
            LocalFrame frame(jni);
            if (!jarLoader.load_jar(InjectableJar_jar.data(), InjectableJar_jar.size()))
                return;
            const Java::Class& MainClass = mujinaClassLoader.findLoadedClass("io/github/lefraudeur/Main");
            if (!MainClass)
            {
                jni.describe_error();
                return;
            }
            const Java::MethodID& main_ID = MainClass.getStaticMethodID("init", "()V");
            Console::log_success("Found Main.init() method, about to invoke...");
            
            main_ID.invoke<void>(MainClass);
            
            
            if (env->ExceptionCheck()) {
                Console::log_error("Java exception occurred during Main.init()");
                env->ExceptionDescribe();
                env->ExceptionClear();
                return;
            }
            
            Console::log_success("Main.init() completed without exceptions");
        }
        
        Console::log_success("Loaded Jar");

        Transformer transformer{ jni, minecraftClassLoader };
        if (!transformer) return;
        transformer.retransform();

        Console::log_success("Retransformed");

        while (!is_uninject_key_pressed())
        {
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
        }

        {
            LocalFrame frame(jni);
            const Java::Class& MainClass = mujinaClassLoader.findLoadedClass("io/github/lefraudeur/Main");
            const Java::MethodID& main_ID = MainClass.getStaticMethodID("shutdown", "()V");
            main_ID.invoke<void>(MainClass);
        }
        Console::log_success("Received end key, waiting 1 second");
    }
    Console::log_success("Garbage collecting");

    
    jni.get_jvmti_env()->ForceGarbageCollection();
    jclass mainClass = jni.find_class_any_cl("io/github/lefraudeur/Main");
    if (!mainClass)
        Console::log_success("Unloaded classes");
    else
        Console::log_error("Failed to unload classes");
}

static void libMain_seh_wrapper(void* modaddr)
{
    __try {
        jni_related(modaddr);
    }
    __except (EXCEPTION_EXECUTE_HANDLER) {
        Console::log_raw("CRITICAL ERROR: Structured Exception occurred (e.g. Access Violation)!");
        Console::log_raw("The process may be unstable. Keeping console open for inspection.");
    }
}

static void libMain(void* modaddr)
{
#ifdef __linux__
    display = XOpenDisplay(NULL);
#endif

    
    // MessageBoxA(NULL, "Mujina libMain starting...", "Mujina DLL", MB_OK | MB_ICONINFORMATION);

    {
        Console console{ modaddr };
        if (!console) return;

        libMain_seh_wrapper(modaddr);

        Console::log_success("Mujina thread finished.");
    }
    

#ifdef __linux__
    XCloseDisplay(display);
#endif
}

#ifdef _WIN32

BOOL WINAPI DllMain(
    HINSTANCE hinstDLL,  
    DWORD fdwReason,     
    LPVOID lpvReserved)  
{
    
    switch (fdwReason)
    {
    case DLL_PROCESS_ATTACH:
        
        
        
        std::thread(libMain, hinstDLL).detach();
        break;

    case DLL_THREAD_ATTACH:
        
        break;

    case DLL_THREAD_DETACH:
        
        break;

    case DLL_PROCESS_DETACH:

        if (lpvReserved != nullptr)
        {
            break; 
        }

        
        break;
    }
    return TRUE;  
}

#elif __linux__

void __attribute__((constructor)) onload_linux()
{
    std::thread(libMain, nullptr).detach();
    return;
}
void __attribute__((destructor)) onunload_linux()
{
    return;
}

#endif
