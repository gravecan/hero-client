using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading;

namespace BloodLoader
{
    class Program
    {
        [DllImport("kernel32.dll")]
        static extern IntPtr OpenProcess(int dwDesiredAccess, bool bInheritHandle, int dwProcessId);

        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        static extern IntPtr GetModuleHandle(string lpModuleName);

        [DllImport("kernel32", CharSet = CharSet.Ansi, ExactSpelling = true, SetLastError = true)]
        static extern IntPtr GetProcAddress(IntPtr hModule, string procName);

        [DllImport("kernel32.dll", SetLastError = true, ExactSpelling = true)]
        static extern IntPtr VirtualAllocEx(IntPtr hProcess, IntPtr lpAddress, uint dwSize, uint flAllocationType, uint flProtect);

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern bool WriteProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer, uint nSize, out UIntPtr lpNumberOfBytesWritten);

        [DllImport("kernel32.dll")]
        static extern IntPtr CreateRemoteThread(IntPtr hProcess, IntPtr lpThreadAttributes, uint dwStackSize, IntPtr lpStartAddress, IntPtr lpParameter, uint dwCreationFlags, IntPtr lpThreadId);

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern bool CloseHandle(IntPtr hObject);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

        [DllImport("user32.dll", SetLastError = true)]
        static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint lpdwProcessId);

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern uint WaitForSingleObject(IntPtr hHandle, uint dwMilliseconds);

        delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

        const int PROCESS_CREATE_THREAD = 0x0002;
        const int PROCESS_QUERY_INFORMATION = 0x0400;
        const int PROCESS_VM_OPERATION = 0x0008;
        const int PROCESS_VM_WRITE = 0x0020;
        const int PROCESS_VM_READ = 0x0010;
        const uint MEM_COMMIT = 0x00001000;
        const uint MEM_RESERVE = 0x00002000;
        const uint PAGE_READWRITE = 4;
        const uint INFINITE = 0xFFFFFFFF;

        static readonly string AUTH_URL = "https://herowin.top/loader_auth.php";
        static readonly string API_KEY = "hR9x$2mKpL7vNw4QdZf8bYcA3jT6eXsU";

        static void SetWhiteBackground()
        {
            Console.BackgroundColor = ConsoleColor.White;
            Console.ForegroundColor = ConsoleColor.Black;
            Console.Clear();
        }

        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                Console.Title = " ";
                Console.CursorVisible = false;
                SetWhiteBackground();

                ServicePointManager.SecurityProtocol = (SecurityProtocolType)3072;

                // ===== GENERATE HWID =====
                string hwid = GetHWID();

                // ===== STEP 1: CHECK HWID IN DB =====
                SetWhiteBackground();
                Console.WriteLine("\n\n\n\n");
                PrintBloodLogo(ConsoleColor.Red);
                Console.WriteLine("\n");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                CenterText("Verifying device...");

                string checkResult = SendRequest("{\"api_key\":\"" + API_KEY + "\",\"action\":\"check_hwid\",\"hwid\":\"" + hwid + "\"}");

                bool authenticated = false;
                string authUsername = "";

                if (checkResult.Contains("\"status\":\"success\""))
                {
                    // HWID found with active license - pass directly
                    authenticated = true;
                    authUsername = ExtractValue(checkResult, "username");

                    SetWhiteBackground();
                    Console.WriteLine("\n\n\n\n");
                    PrintBloodLogo(ConsoleColor.Green);
                    Console.WriteLine("\n");
                    Console.ForegroundColor = ConsoleColor.Green;
                    CenterText("Welcome back, " + authUsername + "!");
                    Thread.Sleep(1500);
                }
                else if (checkResult.Contains("\"status\":\"need_login\""))
                {
                    // HWID not found - need to login
                    // Fall through to login screen
                }
                else
                {
                    // Error (no_license, expired, etc.)
                    ShowAuthError(checkResult);
                    Console.WriteLine("\n");
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    CenterText("Press any key to exit...");
                    Console.ReadKey();
                    return;
                }

                // ===== STEP 2: LOGIN (only if HWID not found) =====
                if (!authenticated)
                {
                    while (!authenticated)
                    {
                        SetWhiteBackground();
                        Console.WriteLine("\n\n");
                        PrintBloodLogo(ConsoleColor.Red);
                        Console.WriteLine("\n");
                        Console.ForegroundColor = ConsoleColor.Red;
                        CenterText("========================================");
                        CenterText("        AUTHENTICATION REQUIRED");
                        CenterText("========================================");
                        Console.WriteLine();
                        Console.ForegroundColor = ConsoleColor.DarkGray;
                        CenterText("This device is not registered.");
                        CenterText("Enter your credentials to link it.");
                        Console.WriteLine("\n");

                        // Username
                        Console.ForegroundColor = ConsoleColor.Red;
                        int pad = (Console.WindowWidth - 40) / 2;
                        if (pad > 0) Console.Write(new string(' ', pad));
                        Console.Write("  Username: ");
                        Console.ForegroundColor = ConsoleColor.Black;
                        Console.CursorVisible = true;
                        string username = Console.ReadLine();
                        Console.CursorVisible = false;

                        if (string.IsNullOrEmpty(username)) continue;

                        // Password
                        Console.ForegroundColor = ConsoleColor.Red;
                        if (pad > 0) Console.Write(new string(' ', pad));
                        Console.Write("  Password: ");
                        Console.ForegroundColor = ConsoleColor.Black;
                        Console.CursorVisible = true;
                        string password = ReadPassword();
                        Console.CursorVisible = false;
                        Console.WriteLine();

                        if (string.IsNullOrEmpty(password)) continue;

                        Console.WriteLine();
                        Console.ForegroundColor = ConsoleColor.DarkGray;
                        CenterText("Verifying...");

                        string json = string.Format("{{\"api_key\":\"{0}\",\"action\":\"login\",\"username\":\"{1}\",\"password\":\"{2}\",\"hwid\":\"{3}\"}}",
                            API_KEY,
                            username.Replace("\"", "\\\""),
                            password.Replace("\"", "\\\""),
                            hwid);

                        string loginResult = SendRequest(json);

                        if (loginResult.Contains("\"status\":\"success\""))
                        {
                            authenticated = true;
                            authUsername = username;

                            SetWhiteBackground();
                            Console.WriteLine("\n\n\n\n");
                            PrintBloodLogo(ConsoleColor.Green);
                            Console.WriteLine("\n");
                            Console.ForegroundColor = ConsoleColor.Green;
                            CenterText("Device linked successfully!");
                            CenterText("Welcome, " + authUsername + "!");
                            Thread.Sleep(1500);
                        }
                        else
                        {
                            ShowAuthError(loginResult);
                            Console.WriteLine("\n");
                            Console.ForegroundColor = ConsoleColor.DarkGray;
                            CenterText("Press any key to retry...");
                            Console.ReadKey(true);
                        }
                    }
                }

                // ===== EXTRACT DLL =====
                string cheatPath = ExtractEmbeddedDll();
                if (string.IsNullOrEmpty(cheatPath))
                    cheatPath = FindCheatExecutable();

                if (string.IsNullOrEmpty(cheatPath))
                {
                    SetWhiteBackground();
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine("\n\n\n");
                    CenterText("========================================");
                    CenterText("DLL NOT FOUND");
                    CenterText("========================================");
                    Console.WriteLine("\n");
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    CenterText("Press any key to exit...");
                    Console.ReadKey();
                    return;
                }

                // ===== WAIT FOR MINECRAFT =====
                Process mcProcess = null;
                bool waiting = true;
                int dots = 0;

                while (waiting)
                {
                    mcProcess = FindMinecraftProcess();
                    if (mcProcess != null)
                    {
                        waiting = false;
                    }
                    else
                    {
                        SetWhiteBackground();
                        Console.WriteLine("\n\n\n\n");
                        PrintBloodLogo(ConsoleColor.Red);
                        Console.WriteLine("\n");
                        Console.ForegroundColor = ConsoleColor.Red;
                        string waitText = "Waiting for Minecraft" + new string('.', dots);
                        CenterText(waitText);
                        dots = (dots + 1) % 4;
                        Thread.Sleep(400);
                    }
                }

                SetWhiteBackground();
                Console.WriteLine("\n\n\n\n");
                PrintBloodLogo(ConsoleColor.Red);
                Console.WriteLine("\n");
                Console.ForegroundColor = ConsoleColor.Red;
                CenterText("========================================");
                Console.ForegroundColor = ConsoleColor.Green;
                CenterText(string.Format("MINECRAFT DETECTED (PID: {0})", mcProcess.Id));
                Console.ForegroundColor = ConsoleColor.Red;
                CenterText("========================================");
                Thread.Sleep(1200);

                SetWhiteBackground();
                Console.WriteLine("\n\n\n\n");
                PrintBloodLogo(ConsoleColor.Red);
                Console.WriteLine("\n");
                Console.ForegroundColor = ConsoleColor.Red;
                CenterText("Blooding...");
                Console.WriteLine();
                DrawCenteredProgressBar(50, 1200, ConsoleColor.Red);

                bool success = InjectDLL(mcProcess, cheatPath);

                SetWhiteBackground();
                Console.WriteLine("\n\n\n\n");
                PrintBloodLogo(success ? ConsoleColor.Green : ConsoleColor.Red);
                Console.WriteLine("\n");

                if (success)
                {
                    Console.ForegroundColor = ConsoleColor.Green;
                    CenterText("Blood Correct");
                }
                else
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    CenterText("Blood Injection Failed");
                }

                Console.WriteLine("\n\n");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                CenterText("PRESS ANY KEY TO EXIT...");
                Console.ReadKey();

                if (cheatPath.Contains(Path.GetTempPath()))
                    try { File.Delete(cheatPath); } catch { }
            }
            catch (Exception ex)
            {
                SetWhiteBackground();
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("\n\n\n");
                CenterText("========================================");
                CenterText("CRITICAL ERROR");
                CenterText("========================================");
                Console.WriteLine("\n");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                CenterText(ex.Message);
                Console.WriteLine("\n\n");
                CenterText("Press any key to exit...");
                Console.ReadKey();
            }
        }

        // ========== HWID ==========
        static string GetHWID()
        {
            try
            {
                string raw = "";
                using (var s = new System.Management.ManagementObjectSearcher("SELECT ProcessorId FROM Win32_Processor"))
                    foreach (var o in s.Get()) { object v = o["ProcessorId"]; if (v != null) raw += v.ToString(); }
                using (var s = new System.Management.ManagementObjectSearcher("SELECT SerialNumber FROM Win32_BaseBoard"))
                    foreach (var o in s.Get()) { object v = o["SerialNumber"]; if (v != null) raw += v.ToString(); }
                using (var s = new System.Management.ManagementObjectSearcher("SELECT SerialNumber FROM Win32_DiskDrive"))
                    foreach (var o in s.Get()) { object v = o["SerialNumber"]; if (v != null) { raw += v.ToString(); break; } }

                using (SHA256 sha = SHA256.Create())
                {
                    byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(raw));
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < hash.Length; i++) sb.Append(hash[i].ToString("x2"));
                    return sb.ToString();
                }
            }
            catch
            {
                string fallback = Environment.MachineName + "-" + Environment.UserName;
                using (SHA256 sha = SHA256.Create())
                {
                    byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(fallback));
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < hash.Length; i++) sb.Append(hash[i].ToString("x2"));
                    return sb.ToString();
                }
            }
        }

        // ========== HTTP ==========
        static string SendRequest(string json)
        {
            try
            {
                using (WebClient client = new WebClient())
                {
                    client.Headers[HttpRequestHeader.ContentType] = "application/json";
                    client.Encoding = Encoding.UTF8;
                    return client.UploadString(AUTH_URL, "POST", json);
                }
            }
            catch (WebException we)
            {
                try
                {
                    using (Stream stream = we.Response.GetResponseStream())
                    using (StreamReader reader = new StreamReader(stream))
                        return reader.ReadToEnd();
                }
                catch { }
                return "{\"status\":\"error\",\"code\":\"connection_error\"}";
            }
            catch
            {
                return "{\"status\":\"error\",\"code\":\"connection_error\"}";
            }
        }

        static string ExtractValue(string json, string key)
        {
            string search = "\"" + key + "\":\"";
            int start = json.IndexOf(search);
            if (start == -1) return "";
            start += search.Length;
            int end = json.IndexOf("\"", start);
            if (end == -1) return "";
            return json.Substring(start, end - start);
        }

        static void ShowAuthError(string response)
        {
            Console.WriteLine("\n");
            Console.ForegroundColor = ConsoleColor.Red;
            CenterText("========================================");

            if (response.Contains("hwid_mismatch"))
            {
                CenterText("  DEVICE NOT AUTHORIZED");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine();
                CenterText("This account is linked to another PC.");
                CenterText("Contact support to reset your HWID.");
            }
            else if (response.Contains("invalid_credentials"))
            {
                CenterText("  INVALID CREDENTIALS");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine();
                CenterText("Wrong username or password.");
            }
            else if (response.Contains("no_license"))
            {
                CenterText("  NO ACTIVE LICENSE");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine();
                CenterText("Purchase a license at herowin.top");
            }
            else if (response.Contains("expired"))
            {
                CenterText("  LICENSE EXPIRED");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine();
                CenterText("Renew your license at herowin.top");
            }
            else
            {
                CenterText("  CONNECTION ERROR");
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine();
                CenterText("Could not connect to auth server.");
            }

            Console.ForegroundColor = ConsoleColor.Red;
            CenterText("========================================");
        }

        static string ReadPassword()
        {
            StringBuilder pass = new StringBuilder();
            while (true)
            {
                ConsoleKeyInfo key = Console.ReadKey(true);
                if (key.Key == ConsoleKey.Enter) break;
                if (key.Key == ConsoleKey.Backspace)
                {
                    if (pass.Length > 0) { pass.Remove(pass.Length - 1, 1); Console.Write("\b \b"); }
                }
                else if (!char.IsControl(key.KeyChar))
                {
                    pass.Append(key.KeyChar);
                    Console.Write("*");
                }
            }
            return pass.ToString();
        }

        // ========== UI ==========
        static void PrintBloodLogo(ConsoleColor color)
        {
            string[] logo = new string[]
            {
                "███████████  █████          ███████    ███████   ██████████ ",
                "░░███░░░░░███░░███          ███░░░░░███ ███░░░░░███░░███░░░░███",
                " ░███    ░███ ░███         ███     ░░███ ███     ░░███░███   ░░███",
                " ░██████████  ░███        ░███      ░███░███      ░███░███    ░███",
                " ░███░░░░░███ ░███        ░███      ░███░███      ░███░███    ░███",
                " ░███    ░███ ░███      █ ░░███     ███ ░░███     ███ ░███    ███ ",
                " ███████████  ███████████  ░░░███████░   ░░░███████░  ██████████  ",
                "░░░░░░░░░░░  ░░░░░░░░░░░     ░░░░░░░       ░░░░░░░   ░░░░░░░░░░   "
            };

            for (int i = 0; i < logo.Length; i++)
            {
                Console.ForegroundColor = color;
                CenterText(logo[i]);
            }
        }

        static void CenterText(string text)
        {
            int windowWidth = Console.WindowWidth;
            int padding = (windowWidth - text.Length) / 2;
            if (padding > 0) Console.Write(new string(' ', padding));
            Console.WriteLine(text);
        }

        static void DrawCenteredProgressBar(int barWidth, int durationMs, ConsoleColor fillColor, int percentage = 0)
        {
            int windowWidth = Console.WindowWidth;
            int leftPadding = (windowWidth - barWidth) / 2;
            if (leftPadding > 0) Console.Write(new string(' ', leftPadding));

            int barStartPos = Console.CursorLeft;
            int barTopPos = Console.CursorTop;

            Console.ForegroundColor = fillColor;
            int fillAmount = (barWidth * percentage) / 100;
            Console.Write(new string('\u2588', fillAmount));
            Console.ForegroundColor = ConsoleColor.Gray;
            Console.Write(new string(' ', barWidth - fillAmount));

            if (durationMs > 0)
            {
                Console.SetCursorPosition(barStartPos, barTopPos);
                Console.ForegroundColor = fillColor;
                int sleepPerStep = durationMs / barWidth;
                for (int i = 0; i < barWidth; i++) { Console.Write("█"); Thread.Sleep(sleepPerStep); }
            }
            Console.WriteLine();
        }

        // ========== MINECRAFT ==========
        static Process FindMinecraftProcess()
        {
            Process r = FindMinecraftByWindow();
            if (r != null) return r;
            r = FindMinecraftByCommandLine();
            if (r != null) return r;
            return FindFirstJavaProcess();
        }

        static Process FindMinecraftByWindow()
        {
            List<Process> list = new List<Process>();
            EnumWindows((hWnd, lParam) =>
            {
                StringBuilder sb = new StringBuilder(256);
                GetWindowText(hWnd, sb, sb.Capacity);
                if (sb.ToString().IndexOf("Minecraft", StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    uint pid; GetWindowThreadProcessId(hWnd, out pid);
                    try { list.Add(Process.GetProcessById((int)pid)); } catch { }
                }
                return true;
            }, IntPtr.Zero);
            return list.Count > 0 ? list[0] : null;
        }

        static Process FindMinecraftByCommandLine()
        {
            foreach (string name in new[] { "javaw", "java" })
                foreach (Process p in Process.GetProcessesByName(name))
                    try
                    {
                        string cmd = GetCommandLine(p);
                        if (cmd.IndexOf("minecraft", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            cmd.IndexOf("net.minecraft", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            cmd.IndexOf("fabric", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            cmd.IndexOf("forge", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            cmd.IndexOf("launchwrapper", StringComparison.OrdinalIgnoreCase) >= 0)
                            return p;
                    }
                    catch { }
            return null;
        }

        static Process FindFirstJavaProcess()
        {
            foreach (string name in new[] { "javaw", "java" })
            {
                Process[] procs = Process.GetProcessesByName(name);
                if (procs.Length > 0)
                {
                    foreach (Process p in procs)
                        try { if (GetCommandLine(p).IndexOf("MinecraftLauncher", StringComparison.OrdinalIgnoreCase) < 0) return p; }
                        catch { return p; }
                    return procs[0];
                }
            }
            return null;
        }

        static string GetCommandLine(Process process)
        {
            try
            {
                using (var s = new System.Management.ManagementObjectSearcher(string.Format("SELECT CommandLine FROM Win32_Process WHERE ProcessId = {0}", process.Id)))
                    foreach (var o in s.Get()) { object cmd = o["CommandLine"]; return cmd != null ? cmd.ToString() : ""; }
            }
            catch { }
            return "";
        }

        // ========== DLL ==========
        static string ExtractEmbeddedDll()
        {
            try
            {
                var asm = Assembly.GetExecutingAssembly();
                using (Stream stream = asm.GetManifestResourceStream("BloodLoader.EmbeddedDll.txt"))
                {
                    if (stream == null) return null;
                    using (StreamReader reader = new StreamReader(stream))
                    {
                        byte[] dll = Convert.FromBase64String(reader.ReadToEnd());
                        string dir = Path.Combine(Path.GetTempPath(), "BL_" + Guid.NewGuid().ToString("N").Substring(0, 8));
                        Directory.CreateDirectory(dir);
                        string path = Path.Combine(dir, "libMujina.dll");
                        File.WriteAllBytes(path, dll);
                        return path;
                    }
                }
            }
            catch { return null; }
        }

        static string FindCheatExecutable()
        {
            string dir = AppDomain.CurrentDomain.BaseDirectory;
            foreach (string name in new[] { "libMujina.dll", "blood.dll", "cheat.dll", "client.dll", "mujina.dll" })
            {
                string path = Path.Combine(dir, name);
                if (File.Exists(path)) return path;
            }
            try
            {
                foreach (string dll in Directory.GetFiles(dir, "*.dll"))
                {
                    string n = Path.GetFileName(dll);
                    if (n.IndexOf("System", StringComparison.OrdinalIgnoreCase) != 0 &&
                        n.IndexOf("Microsoft", StringComparison.OrdinalIgnoreCase) != 0)
                        return dll;
                }
            }
            catch { }
            foreach (string sub in new[] { "bin", "build", "release", "output", "lib" })
            {
                string full = Path.Combine(dir, sub);
                if (Directory.Exists(full))
                    foreach (string name in new[] { "libMujina.dll", "blood.dll", "client.dll" })
                    {
                        string path = Path.Combine(full, name);
                        if (File.Exists(path)) return path;
                    }
            }
            return null;
        }

        static bool InjectDLL(Process target, string dllPath)
        {
            IntPtr hProc = IntPtr.Zero, alloc = IntPtr.Zero, hThread = IntPtr.Zero;
            try
            {
                if (!File.Exists(dllPath)) return false;
                hProc = OpenProcess(PROCESS_CREATE_THREAD | PROCESS_QUERY_INFORMATION | PROCESS_VM_OPERATION | PROCESS_VM_WRITE | PROCESS_VM_READ, false, target.Id);
                if (hProc == IntPtr.Zero) return false;
                IntPtr loadLib = GetProcAddress(GetModuleHandle("kernel32.dll"), "LoadLibraryA");
                if (loadLib == IntPtr.Zero) { CloseHandle(hProc); return false; }
                byte[] pathBytes = Encoding.ASCII.GetBytes(dllPath);
                alloc = VirtualAllocEx(hProc, IntPtr.Zero, (uint)((pathBytes.Length + 1) * Marshal.SizeOf(typeof(char))), MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
                if (alloc == IntPtr.Zero) { CloseHandle(hProc); return false; }
                UIntPtr written;
                if (!WriteProcessMemory(hProc, alloc, pathBytes, (uint)((pathBytes.Length + 1) * Marshal.SizeOf(typeof(char))), out written)) { CloseHandle(hProc); return false; }
                hThread = CreateRemoteThread(hProc, IntPtr.Zero, 0, loadLib, alloc, 0, IntPtr.Zero);
                if (hThread == IntPtr.Zero) { CloseHandle(hProc); return false; }
                WaitForSingleObject(hThread, INFINITE);
                Thread.Sleep(500);
                CloseHandle(hThread); CloseHandle(hProc);
                return true;
            }
            catch
            {
                if (hThread != IntPtr.Zero) CloseHandle(hThread);
                if (hProc != IntPtr.Zero) CloseHandle(hProc);
                return false;
            }
        }
    }
}