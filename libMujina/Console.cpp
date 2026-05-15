#include "Console.hpp"
#include <thread>
#include <fstream>
#include <ctime>
#include <Windows.h>

std::string Console::get_log_path() {
    return "C:\\Users\\Public\\mujina_log.txt";
}

Console::Console(void* dll) : 
	dll(dll),
    buff1(nullptr),
    buff2(nullptr),
    buff3(nullptr)
{
#if defined(_WIN32) && !defined(NO_CONSOLE)
    AllocConsole();
    
    // Ocultar la ventana de consola
    HWND consoleWindow = GetConsoleWindow();
    ShowWindow(consoleWindow, SW_HIDE);
    
    freopen_s(&buff1, "CONOUT$", "w", stdout);
    freopen_s(&buff2, "CONOUT$", "w", stderr);
    freopen_s(&buff3, "CONIN$", "r", stdin);
    log_success("Init");
    
    std::ofstream logFile(get_log_path(), std::ios::trunc);
    if (logFile.is_open()) {
        time_t now = time(0);
        char timestamp[26];
        ctime_s(timestamp, sizeof(timestamp), &now);
        logFile << "=== Mujina Injection Log ===" << '\n';
        logFile << "Started: " << timestamp << '\n';
        logFile.close();
    }
#endif
    _is_error = false;
}

Console::~Console()
{
#if defined(_WIN32) && !defined(NO_CONSOLE)
    if (buff1)
        fclose(buff1);
    if (buff2)
        fclose(buff2);
    if (buff3)
        fclose(buff3);
        
    FreeConsole();
#endif
#if defined(_WIN32)
    FreeLibrary((HMODULE)dll);
#endif
}

void Console::log_warning(const std::string& warning)
{
#ifndef NO_CONSOLE
    std::clog << "[!] " + warning + '\n';
    
    std::ofstream logFile(get_log_path(), std::ios::app);
    if (logFile.is_open()) {
        logFile << "[!] " + warning + '\n';
        logFile.close();
    }
#endif
}

void Console::log_error(const std::string& error)
{
#ifndef NO_CONSOLE
    std::cerr << "[-] " + error + '\n';
    
    std::ofstream logFile(get_log_path(), std::ios::app);
    if (logFile.is_open()) {
        logFile << "[-] ERROR: " + error + '\n';
        logFile.close();
    }
#endif
}

void Console::log_success(const std::string& success)
{
#ifndef NO_CONSOLE
    std::clog << "[+] " + success + '\n';
    
    std::ofstream logFile(get_log_path(), std::ios::app);
    if (logFile.is_open()) {
        logFile << "[+] " + success + '\n';
        logFile.close();
    }
#endif
}

void Console::log_raw(const char* msg)
{
#ifndef NO_CONSOLE
    std::clog << msg << '\n';
    
    std::ofstream logFile(get_log_path(), std::ios::app);
    if (logFile.is_open()) {
        logFile << msg << '\n';
        logFile.close();
    }
#endif
}