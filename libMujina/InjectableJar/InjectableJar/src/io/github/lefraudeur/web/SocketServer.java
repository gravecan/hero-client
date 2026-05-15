package io.github.lefraudeur.web;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.*;
import net.minecraft.client.MinecraftClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketServer extends Thread {
    // CAMBIAR AQUI SI ES NECESARIO
    private static final String RELAY_HOST = "102.129.137.93"; // VPS IP Correcta
    private static final int RELAY_PORT = 8080;
    private static final String RELAY_PATH = "/";
    
    public static final List<Object> clients = new CopyOnWriteArrayList<>(); // Dummy list for compat
    private static SocketServer instance;
    
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean connected = false;
    private final SecureRandom random = new SecureRandom();

    public SocketServer(int port) {
        // Port is ignored in client mode
        instance = this;
    }

    @Override
    public void run() {
        while (true) {
            try {
                connect();
                // If disconnected, wait before retry
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void connect() throws Exception {
        System.out.println("[Relay] Connecting to " + RELAY_HOST + ":" + RELAY_PORT);
        socket = new Socket(RELAY_HOST, RELAY_PORT);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // 1. Handshake
        String secretKey = generateKey();
        String request = "GET " + RELAY_PATH + " HTTP/1.1\r\n" +
                "Host: " + RELAY_HOST + ":" + RELAY_PORT + "\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Key: " + secretKey + "\r\n" +
                "Sec-WebSocket-Version: 13\r\n\r\n";
        
        out.write(request.getBytes(StandardCharsets.UTF_8));
        out.flush();

        // 2. Read Response
        Scanner s = new Scanner(in, "UTF-8");
        String header = s.useDelimiter("\\r\\n\\r\\n").next();
        if (!header.contains("101 Switching Protocols")) {
            System.err.println("[Relay] Handshake failed: " + header);
            socket.close();
            return;
        }

        System.out.println("[Relay] Connected!");
        connected = true;

        // 3. Identify
        String username = "Unknown";
        try {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getSession() != null) {
                username = MinecraftClient.getInstance().getSession().getUsername();
            }
        } catch (Exception ignored) {}
        
        sendJson(String.format("{\"type\":\"identify\",\"role\":\"client\",\"user\":\"%s\"}", username));

        // 4. Listen Loop
        while (connected && !socket.isClosed()) {
            int b = in.read();
            if (b == -1) break;

            boolean fin = (b & 0x80) != 0;
            int opcode = b & 0x0F;

            if (opcode == 8) { // Close
                break;
            }

            b = in.read();
            boolean masked = (b & 0x80) != 0;
            long len = b & 0x7F;

            if (len == 126) {
                len = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
            } else if (len == 127) {
                 in.readNBytes(8); 
                 len = 0; 
            }

            if (masked) {
                in.readNBytes(4); 
            }
            
            byte[] payload = new byte[(int)len];
            int read = 0;
            while(read < len) {
                read += in.read(payload, read, (int)len - read);
            }

            if (opcode == 1) { // Text
                String msg = new String(payload, StandardCharsets.UTF_8);
                handleMessage(msg);
            }
        }
        
        connected = false;
        socket.close();
    }

    private String generateKey() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void broadcast(String message) {
        if (instance != null) instance.sendJson(message);
    }
    
    private synchronized void sendJson(String message) {
        if (!connected) return;
        try {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            out.write(encodeFrame(data));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        }
    }

    private byte[] encodeFrame(byte[] data) {
        int len = data.length;
        int headerLen;
        
        if (len <= 125) headerLen = 2;
        else if (len <= 65535) headerLen = 4;
        else headerLen = 10;
        
        headerLen += 4; // Masking key is mandatory for client -> server
        
        byte[] frame = new byte[headerLen + len];
        
        frame[0] = (byte) 0x81; // FIN + Text
        
        if (len <= 125) {
            frame[1] = (byte) (0x80 | len); // Masked
        } else if (len <= 65535) {
            frame[1] = (byte) (0x80 | 126);
            frame[2] = (byte) (len >> 8);
            frame[3] = (byte) (len & 0xFF);
        } else {
            frame[1] = (byte) (0x80 | 127);
            frame[6] = (byte) (len >> 24);
            frame[7] = (byte) (len >> 16);
            frame[8] = (byte) (len >> 8);
            frame[9] = (byte) (len & 0xFF);
        }
        
        byte[] mask = new byte[4];
        random.nextBytes(mask);
        int maskStart = frame.length - len - 4;
        System.arraycopy(mask, 0, frame, maskStart, 4);
        
        for (int i = 0; i < len; i++) {
            frame[maskStart + 4 + i] = (byte) (data[i] ^ mask[i % 4]);
        }
        
        return frame;
    }

    private void handleMessage(String msg) {
        try {
            if (msg.contains("get_state")) {
                 sendJson(buildStateUpdate());
            } 
            else if (msg.contains("toggle_module")) {
                 String mod = extract(msg, "module");
                 String enStr = extract(msg, "enabled");
                 boolean enabled = enStr.contains("true");
                 
                 for(Module m : Main.modules) {
                     if(m.getName().equals(mod)) {
                         if(enabled && !m.isEnabled()) m.enable();
                         else if(!enabled && m.isEnabled()) m.disable();
                     }
                 }
                 sendJson(buildStateUpdate());
            }
            else if (msg.contains("update_setting")) {
                String mod = extract(msg, "module");
                String set = extract(msg, "setting");
                String val = extract(msg, "value"); 
                
                for(Module m : Main.modules) {
                    if(m.getName().equals(mod)) {
                        Setting<?> s = m.getSettingByName(set);
                        if(s != null) {
                            if(s instanceof BooleanSetting) ((BooleanSetting)s).setValue(val.contains("true"));
                            else if(s instanceof NumberSetting) ((NumberSetting)s).setValue(Double.parseDouble(val));
                            else if(s instanceof ModeSetting) ((ModeSetting)s).setValue(Integer.parseInt(val));
                            else if(s instanceof KeybindSetting) ((KeybindSetting)s).setValue(Integer.parseInt(val));
                            // MultiChoice too complex for simple parser, ignore for now
                        }
                    }
                }
                sendJson(buildStateUpdate());
            }
            else if (msg.contains("update_keybind")) {
                String mod = extract(msg, "module");
                String key = extract(msg, "key");
                int keyBind = Integer.parseInt(key);
                
                for(Module m : Main.modules) {
                    if(m.getName().equals(mod)) {
                        m.setKey(keyBind);
                    }
                }
                sendJson(buildStateUpdate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Very basic JSON value extractor
    private String extract(String json, String key) {
        String search = "\"" + key + "\":";
        int i = json.indexOf(search);
        if(i == -1) return "";
        i += search.length();
        
        char first = json.charAt(i);
        if(first == '"') {
            int end = json.indexOf("\"", i+1);
            return json.substring(i+1, end);
        } else {
            int end = json.indexOf(",", i);
            if(end == -1) end = json.indexOf("}", i);
            return json.substring(i, end).trim();
        }
    }

    // --- Legacy Static Methods for Broadcast ---
    public static void log(String level, String message) {
        if (instance != null) {
            String json = String.format("{\"type\":\"log\",\"level\":\"%s\",\"message\":\"%s\"}", level, message.replace("\"", "\\\""));
            instance.sendJson(json);
        }
    }
    
    public static String buildStateUpdate() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"state_update\",\"modules\":[");

        Module[] modules = Main.modules;
        for (int i = 0; i < modules.length; i++) {
            Module m = modules[i];
            sb.append("{");
            sb.append("\"name\":\"").append(m.getName()).append("\",");
            sb.append("\"category\":\"").append(m.getCategory().getName()).append("\",");
            sb.append("\"enabled\":").append(m.isEnabled()).append(",");
            sb.append("\"key_bind\":").append(m.getKeyBind()).append(",");
            sb.append("\"settings\":[");
            List<Setting<?>> settings = m.getSettings();
            boolean firstSetting = true;
            for (int j = 0; j < settings.size(); j++) {
                Setting<?> s = settings.get(j);
                if (s.isHidden()) continue;
                if (!firstSetting) sb.append(",");
                sb.append("{");
                sb.append("\"name\":\"").append(s.getName()).append("\",");
                sb.append("\"type\":\"").append(s.getType()).append("\",");

                if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    sb.append("\"value\":").append(ns.getValue()).append(",");
                    sb.append("\"min\":").append(ns.getMin()).append(",");
                    sb.append("\"max\":").append(ns.getMax());
                } else if (s instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) s;
                    sb.append("\"value\":").append(ms.getValue()).append(",");
                    sb.append("\"modes\":[");
                    for (int k = 0; k < ms.getModes().size(); k++) {
                        sb.append("\"").append(ms.getModes().get(k)).append("\"");
                        if (k < ms.getModes().size() - 1)
                            sb.append(",");
                    }
                    sb.append("]");
                } else if (s instanceof MultiChoiceSetting) {
                    MultiChoiceSetting mcs = (MultiChoiceSetting) s;
                    sb.append("\"value\":[");
                    List<String> values = mcs.getValue();
                    for (int k = 0; k < values.size(); k++) {
                        sb.append("\"").append(values.get(k)).append("\"");
                        if (k < values.size() - 1) sb.append(",");
                    }
                    sb.append("],\"options\":[");
                    List<String> options = mcs.getOptions();
                    for (int k = 0; k < options.size(); k++) {
                        sb.append("\"").append(options.get(k)).append("\"");
                        if (k < options.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                } else {
                    sb.append("\"value\":").append(s.getValue());
                }
                sb.append("}");
                firstSetting = false;
            }
            sb.append("]");
            sb.append("}");

            if (i < modules.length - 1)
                sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }
}
