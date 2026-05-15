package io.github.lefraudeur.web;

public class WebContent {
    public static final String HTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Blood v2</title>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600;700&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --red-main: #DC2626;
                        --red-bright: #EF4444;
                        --red-dark: #991B1B;
                        --red-glow: rgba(220, 38, 38, 0.15);
                        --white: #FFFFFF;
                        --white-dim: #F5F5F5;
                        --iron: #71717A;
                        --iron-light: #A1A1AA;
                        --iron-dark: #52525B;
                        --bg-main: #09090B;
                        --bg-elevated: #18181B;
                        --bg-card: #1F1F23;
                        --border: #27272A;
                        --border-hover: #3F3F46;
                    }

                    * { box-sizing: border-box; margin: 0; padding: 0; cursor: default; }

                    body {
                        background: var(--bg-main);
                        color: var(--white);
                        height: 100vh;
                        display: flex;
                        flex-direction: column;
                        font-family: 'Inter', sans-serif;
                        overflow: hidden;
                    }

                    .topbar {
                        height: 56px;
                        background: var(--bg-elevated);
                        border-bottom: 1px solid var(--border);
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 0 28px;
                        flex-shrink: 0;
                    }

                    .logo {
                        font-size: 1.4rem;
                        font-weight: 800;
                        color: var(--white);
                        letter-spacing: 2px;
                        display: flex;
                        align-items: center;
                        gap: 10px;
                    }

                    .logo::before {
                        content: '';
                        width: 8px;
                        height: 8px;
                        background: var(--red-main);
                        border-radius: 50%;
                        box-shadow: 0 0 12px var(--red-main);
                    }

                    .logo-version {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.6rem;
                        font-weight: 600;
                        color: var(--iron);
                        background: var(--bg-main);
                        border: 1px solid var(--border);
                        padding: 3px 8px;
                        border-radius: 4px;
                        letter-spacing: 1px;
                    }

                    .status-pill {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.65rem;
                        font-weight: 700;
                        letter-spacing: 1.2px;
                        padding: 6px 14px;
                        border-radius: 6px;
                        display: flex;
                        align-items: center;
                        gap: 7px;
                        transition: all 0.3s;
                        border: 1px solid var(--border);
                    }

                    .status-pill.offline {
                        color: var(--iron);
                        background: var(--bg-main);
                    }

                    .status-pill.online {
                        color: var(--red-main);
                        background: var(--red-glow);
                        border-color: var(--red-dark);
                    }

                    .status-dot {
                        width: 6px;
                        height: 6px;
                        border-radius: 50%;
                        background: currentColor;
                    }

                    .status-pill.online .status-dot {
                        animation: pulse 2s infinite;
                    }

                    @keyframes pulse {
                        0%, 100% { opacity: 1; transform: scale(1); }
                        50% { opacity: 0.6; transform: scale(0.85); }
                    }

                    .layout { flex: 1; display: flex; overflow: hidden; }

                    .sidebar {
                        width: 200px;
                        background: var(--bg-elevated);
                        border-right: 1px solid var(--border);
                        display: flex;
                        flex-direction: column;
                        flex-shrink: 0;
                        padding: 20px 0;
                    }

                    .sidebar-group { margin-bottom: 20px; }

                    .sidebar-label {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.55rem;
                        letter-spacing: 1.5px;
                        color: var(--iron-dark);
                        padding: 0 16px;
                        margin-bottom: 8px;
                        text-transform: uppercase;
                        font-weight: 600;
                    }

                    .nav-btn {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        padding: 10px 16px;
                        font-size: 0.85rem;
                        font-weight: 600;
                        color: var(--iron-light);
                        letter-spacing: 0.2px;
                        transition: all 0.2s;
                        border-left: 2px solid transparent;
                        margin: 0 8px;
                        border-radius: 6px;
                    }

                    .nav-btn:hover {
                        color: var(--white);
                        background: rgba(255,255,255,0.03);
                        cursor: pointer;
                    }

                    .nav-btn.active {
                        color: var(--white);
                        background: var(--red-glow);
                        border-left-color: var(--red-main);
                    }

                    .nav-btn i { font-size: 0.82rem; width: 16px; text-align: center; }

                    .nav-count {
                        margin-left: auto;
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.6rem;
                        font-weight: 600;
                        color: var(--iron-dark);
                        background: var(--bg-main);
                        border: 1px solid var(--border);
                        padding: 2px 7px;
                        border-radius: 4px;
                    }

                    .nav-btn.active .nav-count {
                        color: var(--red-main);
                        background: rgba(220, 38, 38, 0.1);
                        border-color: var(--red-dark);
                    }

                    .sidebar-divider { height: 1px; background: var(--border); margin: 16px 16px; }

                    .sidebar-footer {
                        margin-top: auto;
                        padding: 12px 16px;
                        border-top: 1px solid var(--border);
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.6rem;
                        color: var(--iron-dark);
                        letter-spacing: 0.8px;
                    }

                    .main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

                    .main-header {
                        padding: 20px 28px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        flex-shrink: 0;
                        border-bottom: 1px solid var(--border);
                        background: var(--bg-elevated);
                    }

                    .section-name {
                        font-size: 1.4rem;
                        font-weight: 800;
                        color: var(--white);
                        letter-spacing: 0.5px;
                        display: flex;
                        align-items: center;
                        gap: 12px;
                    }

                    .section-count {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.7rem;
                        color: var(--iron);
                        font-weight: 500;
                    }

                    .modules-scroll {
                        flex: 1;
                        overflow-y: auto;
                        padding: 24px;
                        scrollbar-width: thin;
                        scrollbar-color: var(--border) transparent;
                    }

                    .modules-scroll::-webkit-scrollbar { width: 4px; }
                    .modules-scroll::-webkit-scrollbar-track { background: transparent; }
                    .modules-scroll::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }

                    .module-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                        gap: 14px;
                    }

                    .module-card {
                        background: var(--bg-card);
                        border: 1px solid var(--border);
                        border-radius: 8px;
                        overflow: hidden;
                        transition: all 0.2s;
                        animation: fadeIn 0.3s ease both;
                    }

                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(8px); }
                        to { opacity: 1; transform: translateY(0); }
                    }

                    .module-card:hover {
                        border-color: var(--border-hover);
                        transform: translateY(-1px);
                    }

                    .module-card.enabled {
                        border-color: var(--red-dark);
                        box-shadow: 0 0 0 1px var(--red-dark);
                    }

                    .card-head {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 14px 16px;
                        background: var(--bg-elevated);
                        border-bottom: 1px solid var(--border);
                    }

                    .card-left { display: flex; align-items: center; gap: 12px; }

                    .card-icon {
                        width: 32px;
                        height: 32px;
                        background: var(--bg-main);
                        border: 1px solid var(--border);
                        border-radius: 6px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 0.85rem;
                        color: var(--iron);
                        transition: all 0.2s;
                    }

                    .module-card.enabled .card-icon {
                        background: var(--red-glow);
                        border-color: var(--red-dark);
                        color: var(--red-main);
                    }

                    .card-name {
                        font-size: 0.95rem;
                        font-weight: 700;
                        color: var(--white);
                        letter-spacing: 0.3px;
                    }

                    .card-right { display: flex; align-items: center; gap: 8px; }

                    .kb-btn {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.6rem;
                        font-weight: 700;
                        letter-spacing: 0.8px;
                        color: var(--iron);
                        background: var(--bg-main);
                        border: 1px solid var(--border);
                        padding: 4px 10px;
                        border-radius: 4px;
                        min-width: 42px;
                        text-align: center;
                        cursor: pointer;
                        transition: all 0.2s;
                    }

                    .kb-btn:hover {
                        border-color: var(--red-main);
                        color: var(--red-main);
                    }

                    .kb-btn.recording {
                        border-color: var(--red-main);
                        color: var(--red-main);
                        background: var(--red-glow);
                        animation: blink 0.8s infinite;
                    }

                    @keyframes blink { 50% { opacity: 0.6; } }

                    .tog { position: relative; width: 40px; height: 22px; flex-shrink: 0; }
                    .tog input { opacity: 0; width: 0; height: 0; }

                    .tog-track {
                        position: absolute;
                        inset: 0;
                        background: var(--bg-main);
                        border: 1px solid var(--border);
                        border-radius: 11px;
                        cursor: pointer;
                        transition: all 0.2s;
                    }

                    .tog-thumb {
                        position: absolute;
                        width: 16px;
                        height: 16px;
                        background: var(--iron-dark);
                        border-radius: 50%;
                        top: 2px;
                        left: 2px;
                        transition: all 0.2s;
                        pointer-events: none;
                    }

                    .tog input:checked ~ .tog-track {
                        background: var(--red-dark);
                        border-color: var(--red-main);
                    }

                    .tog input:checked ~ .tog-track .tog-thumb {
                        transform: translateX(18px);
                        background: var(--red-main);
                    }

                    .card-divider { height: 1px; background: var(--border); }

                    .card-body { padding: 16px; display: flex; flex-direction: column; gap: 12px; }

                    .s-row { display: flex; flex-direction: column; gap: 8px; }

                    .s-row.bool {
                        flex-direction: row;
                        align-items: center;
                        justify-content: space-between;
                        padding: 10px 12px;
                        background: var(--bg-elevated);
                        border-radius: 6px;
                        border: 1px solid var(--border);
                        transition: all 0.2s;
                    }

                    .s-row.bool:hover { border-color: var(--border-hover); }

                    .s-row.bool.on {
                        background: var(--red-glow);
                        border-color: var(--red-dark);
                    }

                    .s-name {
                        font-size: 0.75rem;
                        font-weight: 600;
                        color: var(--iron-light);
                        letter-spacing: 0.2px;
                        font-family: 'JetBrains Mono', monospace;
                    }

                    .s-row.bool.on .s-name { color: var(--white); }

                    .s-head { display: flex; justify-content: space-between; align-items: center; }

                    .s-val {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.8rem;
                        font-weight: 700;
                        color: var(--red-main);
                    }

                    .s-track {
                        position: relative;
                        height: 5px;
                        background: var(--bg-elevated);
                        border-radius: 3px;
                        border: 1px solid var(--border);
                    }

                    .s-fill {
                        position: absolute;
                        height: 100%;
                        background: var(--red-main);
                        border-radius: 2px;
                        pointer-events: none;
                    }

                    input[type=range] {
                        -webkit-appearance: none;
                        position: absolute;
                        width: 100%;
                        height: 100%;
                        top: 0;
                        left: 0;
                        background: transparent;
                        outline: none;
                        cursor: pointer;
                        margin: 0;
                    }

                    input[type=range]::-webkit-slider-runnable-track {
                        height: 5px;
                        background: transparent;
                        border-radius: 3px;
                    }

                    input[type=range]::-webkit-slider-thumb {
                        -webkit-appearance: none;
                        width: 14px;
                        height: 14px;
                        border-radius: 50%;
                        background: var(--white);
                        border: 2px solid var(--red-main);
                        margin-top: -5px;
                        cursor: pointer;
                        transition: all 0.15s;
                    }

                    input[type=range]:active::-webkit-slider-thumb {
                        transform: scale(1.2);
                    }

                    .d-track {
                        position: relative;
                        height: 5px;
                        background: var(--bg-elevated);
                        border-radius: 3px;
                        border: 1px solid var(--border);
                        margin: 4px 0;
                    }

                    .d-fill {
                        position: absolute;
                        height: 100%;
                        background: var(--red-main);
                        border-radius: 2px;
                        pointer-events: none;
                    }

                    .d-thumb {
                        width: 14px;
                        height: 14px;
                        background: var(--white);
                        border: 2px solid var(--red-main);
                        border-radius: 50%;
                        position: absolute;
                        top: -5px;
                        transform: translateX(-50%);
                        cursor: pointer;
                        z-index: 2;
                        transition: all 0.15s;
                    }

                    .d-thumb:hover { transform: translateX(-50%) scale(1.15); }

                    .m-row { display: flex; gap: 6px; }

                    .m-btn {
                        flex: 1;
                        padding: 8px;
                        background: var(--bg-elevated);
                        border: 1px solid var(--border);
                        border-radius: 6px;
                        color: var(--iron-light);
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.62rem;
                        font-weight: 700;
                        letter-spacing: 0.3px;
                        text-align: center;
                        cursor: pointer;
                        transition: all 0.2s;
                    }

                    .m-btn:hover { border-color: var(--border-hover); color: var(--white); }

                    .m-btn.active {
                        background: var(--red-glow);
                        border-color: var(--red-dark);
                        color: var(--red-main);
                    }

                    .bool-group { display: flex; flex-direction: column; gap: 6px; }

                    .mc-row { display: flex; flex-direction: column; gap: 6px; }
                    .mc-head {
                        background: var(--bg-elevated);
                        border: 1px solid var(--border);
                        border-radius: 6px;
                        padding: 10px 12px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        cursor: pointer;
                        transition: all 0.2s;
                    }
                    .mc-head:hover { border-color: var(--border-hover); }
                    .mc-head .fa-chevron-down { font-size: 0.7rem; transition: transform 0.3s; color: var(--iron); }
                    .mc-head.open .fa-chevron-down { transform: rotate(180deg); }
                    .mc-opts {
                        display: none;
                        flex-direction: column;
                        gap: 3px;
                        padding: 4px;
                        background: var(--bg-elevated);
                        border-radius: 6px;
                        border: 1px solid var(--border);
                    }
                    .mc-opts.open { display: flex; }
                    .mc-opt {
                        padding: 8px 12px;
                        border-radius: 4px;
                        font-size: 0.75rem;
                        color: var(--iron-light);
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        cursor: pointer;
                        transition: all 0.2s;
                    }
                    .mc-opt:hover { background: rgba(255,255,255,0.03); color: var(--white); }
                    .mc-opt.active { color: var(--red-main); background: var(--red-glow); }
                    .mc-opt .fa-check { font-size: 0.7rem; opacity: 0; }
                    .mc-opt.active .fa-check { opacity: 1; }

                    .s-kb-row {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 10px 12px;
                        background: var(--bg-elevated);
                        border-radius: 6px;
                        border: 1px solid var(--border);
                    }
                    .s-kb-btn {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.65rem;
                        color: var(--red-main);
                        background: var(--red-glow);
                        border: 1px solid var(--red-dark);
                        padding: 4px 10px;
                        border-radius: 4px;
                        cursor: pointer;
                        min-width: 48px;
                        text-align: center;
                        transition: all 0.2s;
                        font-weight: 700;
                    }
                    .s-kb-btn:hover { border-color: var(--red-main); }
                    .s-kb-btn.recording { animation: blink 0.8s infinite; }

                    .console {
                        width: 240px;
                        background: var(--bg-elevated);
                        border-left: 1px solid var(--border);
                        display: flex;
                        flex-direction: column;
                        flex-shrink: 0;
                    }

                    .con-head {
                        padding: 16px;
                        border-bottom: 1px solid var(--border);
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                    }

                    .con-title {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.6rem;
                        font-weight: 700;
                        letter-spacing: 1.5px;
                        color: var(--iron-dark);
                        text-transform: uppercase;
                    }

                    .con-logs {
                        flex: 1;
                        overflow-y: auto;
                        padding: 10px;
                        display: flex;
                        flex-direction: column;
                        gap: 5px;
                        scrollbar-width: thin;
                        scrollbar-color: var(--border) transparent;
                    }

                    .con-logs::-webkit-scrollbar { width: 4px; }
                    .con-logs::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }

                    .log {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.64rem;
                        line-height: 1.5;
                        padding: 6px 10px;
                        border-radius: 4px;
                        background: var(--bg-card);
                        border: 1px solid var(--border);
                        display: flex;
                        gap: 8px;
                        align-items: flex-start;
                    }

                    .log-t { color: var(--iron-dark); flex-shrink: 0; }
                    .log-i { color: var(--iron-light); }
                    .log-e { color: var(--red-main); }
                    .log-s { color: #10B981; }
                    .log-w { color: #F59E0B; }

                    .empty {
                        grid-column: 1/-1;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 200px;
                        gap: 12px;
                    }

                    .empty i { font-size: 2rem; color: var(--border-hover); }
                    .empty span {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.7rem;
                        letter-spacing: 1px;
                        color: var(--iron-dark);
                    }
                </style>
            </head>
            <body>

                <div class="topbar">
                    <div class="logo">
                        BLOOD
                        <span class="logo-version">v2</span>
                    </div>
                    <div id="status" class="status-pill offline">
                        <div class="status-dot"></div>
                        <span id="status-text">OFFLINE</span>
                    </div>
                </div>

                <div class="layout">

                    <div class="sidebar">
                        <div class="sidebar-group">
                            <div class="sidebar-label">Modules</div>
                            <div class="nav-btn active" onclick="setCategory('Combat',this)">
                                <i class="fa-solid fa-crosshairs"></i> Combat
                                <span class="nav-count" id="cnt-Combat">0</span>
                            </div>
                            <div class="nav-btn" onclick="setCategory('Visual',this)">
                                <i class="fa-solid fa-eye"></i> Visual
                                <span class="nav-count" id="cnt-Visual">0</span>
                            </div>
                            <div class="nav-btn" onclick="setCategory('Movement',this)">
                                <i class="fa-solid fa-person-running"></i> Movement
                                <span class="nav-count" id="cnt-Movement">0</span>
                            </div>
                            <div class="nav-btn" onclick="setCategory('Macros',this)">
                                <i class="fa-solid fa-wrench"></i> Macros
                                <span class="nav-count" id="cnt-Macros">0</span>
                            </div>
                        </div>
                        <div class="sidebar-divider"></div>
                        <div class="sidebar-group">
                            <div class="sidebar-label">View</div>
                            <div class="nav-btn" onclick="setCategory('All',this)">
                                <i class="fa-solid fa-table-cells"></i> All Modules
                            </div>
                        </div>
                        <div class="sidebar-footer">
                            <i class="fa-solid fa-code-branch" style="margin-right:5px;"></i>
                            build 2026
                        </div>
                    </div>

                    <div class="main">
                        <div class="main-header">
                            <div class="section-name">
                                <span id="section-label">Combat</span>
                                <span class="section-count" id="section-count">0 modules</span>
                            </div>
                        </div>
                        <div class="modules-scroll">
                            <div id="grid" class="module-grid"></div>
                        </div>
                    </div>

                    <div class="console">
                        <div class="con-head">
                            <span class="con-title">Console</span>
                            <i class="fa-solid fa-terminal" style="color:var(--iron-dark);font-size:0.7rem;"></i>
                        </div>
                        <div class="con-logs" id="logs"></div>
                    </div>

                </div>

                <script>
                    const grid     = document.getElementById('grid');
                    const statusEl = document.getElementById('status');
                    const statusTx = document.getElementById('status-text');
                    const logsEl   = document.getElementById('logs');

                    let socket, config = [], isConnected = false, currentCat = 'Combat';

                    const GLFW = {
                        'KeyA':65,'KeyB':66,'KeyC':67,'KeyD':68,'KeyE':69,'KeyF':70,'KeyG':71,'KeyH':72,
                        'KeyI':73,'KeyJ':74,'KeyK':75,'KeyL':76,'KeyM':77,'KeyN':78,'KeyO':79,'KeyP':80,
                        'KeyQ':81,'KeyR':82,'KeyS':83,'KeyT':84,'KeyU':85,'KeyV':86,'KeyW':87,'KeyX':88,
                        'KeyY':89,'KeyZ':90,'Digit0':48,'Digit1':49,'Digit2':50,'Digit3':51,'Digit4':52,
                        'Digit5':53,'Digit6':54,'Digit7':55,'Digit8':56,'Digit9':57,'Space':32,
                        'Escape':256,'Enter':257,'Tab':258,'Backspace':259,'CapsLock':280,
                        'F1':290,'F2':291,'F3':292,'F4':293,'F5':294,'F6':295,'F7':296,'F8':297,
                        'F9':298,'F10':299,'F11':300,'F12':301,'ShiftLeft':340,'ControlLeft':341,
                        'AltLeft':342,'ShiftRight':344,'ControlRight':345,'AltRight':346
                    };

                    const CAT_MAP = {
                        'COMBAT':'Combat','VISUAL':'Visual','RENDER':'Visual',
                        'MOVEMENT':'Movement','MACROS':'Macros','PLAYER':'Macros',
                        'WORLD':'Macros','MISC':'Macros','EXPLOIT':'Macros'
                    };

                    const ICONS = {
                        'TriggerBot':'fa-crosshairs','AimAssist':'fa-bullseye','KillAura':'fa-bolt',
                        'AutoClicker':'fa-hand-pointer','Velocity':'fa-wind','Sprint':'fa-person-running',
                        'Scaffold':'fa-cubes','ESP':'fa-eye','NoFall':'fa-feather','Speed':'fa-gauge-high',
                        'Reach':'fa-arrows-left-right','Hitbox':'fa-expand',
                        'KeyPearl':'fa-regular fa-circle-dot',
                        'FastEXP':'fa-solid fa-bottle-droplet',
                        'FastPot':'fa-solid fa-flask',
                        'FastInventoryMove':'fa-solid fa-box-open'
                    };

                    function keyName(c) {
                        if (!c || c <= 0) return 'NONE';
                        for (const [n,v] of Object.entries(GLFW)) {
                            if (v === c) return n.replace(/Key|Digit|Left|Right/g,'').toUpperCase();
                        }
                        return 'K'+c;
                    }

                    function modCat(m) {
                        return m.category ? (CAT_MAP[m.category.toUpperCase()] || 'Macros') : 'Macros';
                    }

                    function log(type, msg) {
                        const d = document.createElement('div');
                        d.className = 'log';
                        const t = new Date().toLocaleTimeString([],{hour:'2-digit',minute:'2-digit',second:'2-digit'});
                        d.innerHTML = `<span class="log-t">${t}</span><span class="log-${type}">${msg}</span>`;
                        logsEl.appendChild(d);
                        logsEl.scrollTop = logsEl.scrollHeight;
                        if (logsEl.children.length > 80) logsEl.removeChild(logsEl.firstChild);
                    }

                    function setCategory(cat, el) {
                        currentCat = cat;
                        document.querySelectorAll('.nav-btn').forEach(n => n.classList.remove('active'));
                        el.classList.add('active');
                        document.getElementById('section-label').textContent = cat === 'All' ? 'All Modules' : cat;
                        render();
                    }

                    function updateCounts() {
                        ['Combat','Visual','Movement','Macros'].forEach(c => {
                            const el = document.getElementById('cnt-'+c);
                            if (el) el.textContent = config.filter(m => modCat(m) === c).length;
                        });
                    }

                    function connect() {
                        log('i', 'Connecting to localhost:6969...');
                        socket = new WebSocket('ws://localhost:6969');
                        socket.onopen = () => {
                            isConnected = true;
                            statusEl.className = 'status-pill online';
                            statusTx.textContent = 'CONNECTED';
                            log('s', 'Connection established.');
                            send({ type:'get_state' });
                        };
                        socket.onclose = () => {
                            isConnected = false;
                            statusEl.className = 'status-pill offline';
                            statusTx.textContent = 'OFFLINE';
                            log('e', 'Connection lost. Retrying in 2s...');
                            setTimeout(connect, 2000);
                        };
                        socket.onmessage = (e) => {
                            const d = JSON.parse(e.data);
                            if (d.type === 'state_update') sync(d.modules);
                            else if (d.type === 'log') log(d.level, d.message);
                        };
                    }

                    function send(o) { if (isConnected) socket.send(JSON.stringify(o)); }

                    function sync(mods) {
                        if (window.dragging) return;
                        const changed = JSON.stringify(mods.map(m=>m.name)) !== JSON.stringify(config.map(m=>m.name));
                        if (changed) { config = mods; updateCounts(); render(); return; }

                        mods.forEach((m, i) => {
                            const old = config[i];
                            const card = grid.querySelector(`.module-card[data-mod="${m.name}"]`);
                            if (!card) return;

                            if (m.enabled !== old.enabled) {
                                card.classList.toggle('enabled', m.enabled);
                                const inp = card.querySelector('.card-head .tog input');
                                if (inp) inp.checked = m.enabled;
                            }

                            if (m.key_bind !== old.key_bind) {
                                const btn = card.querySelector('.kb-btn');
                                if (btn && !btn.classList.contains('recording')) btn.textContent = keyName(m.key_bind);
                            }

                             m.settings.forEach(s => {
                                 const os = old.settings.find(x => x.name === s.name);
                                 if (!os || JSON.stringify(s.value) === JSON.stringify(os.value)) return;
                                 const row = card.querySelector(`.s-row[data-s="${s.name}"]`);
                                 if (!row) return;
                                 if (s.type === 'bool') {
                                     const inp = row.querySelector('.tog input');
                                     if (inp) { inp.checked = s.value; row.classList.toggle('on', s.value); }
                                 } else if (s.type === 'keybind') {
                                     const btn = row.querySelector('.s-kb-btn');
                                     if (btn && !btn.classList.contains('recording')) btn.textContent = keyName(s.value);
                                 } else if (s.type === 'multichoice') {
                                     const head = row.querySelector('.mc-head .s-val');
                                     if (head) head.textContent = s.value.length ? s.value.join(', ') : 'None';
                                     row.querySelectorAll('.mc-opt').forEach(opt => {
                                         const name = opt.getAttribute('data-opt');
                                         opt.classList.toggle('active', s.value.includes(name));
                                     });
                                 } else {
                                     const sv = row.querySelector('.s-val');
                                     if (sv) sv.textContent = s.value;
                                     const inp = row.querySelector('input[type=range]');
                                     if (inp) { inp.value = s.value; fillSlider(inp); }
                                 }
                             });
                        });
                        config = mods;
                    }

                    function render() {
                        grid.innerHTML = '';
                        const list = currentCat === 'All' ? config : config.filter(m => modCat(m) === currentCat);
                        document.getElementById('section-count').textContent = list.length + ' module' + (list.length !== 1 ? 's' : '');

                        if (!list.length) {
                            grid.innerHTML = '<div class="empty"><i class="fa-solid fa-box-open"></i><span>No modules here</span></div>';
                            return;
                        }
                        list.forEach((m, i) => grid.appendChild(buildCard(m, i)));
                    }

                    function fillSlider(inp) {
                        const pct = ((inp.value - inp.min) / (inp.max - inp.min)) * 100;
                        const fill = inp.previousElementSibling;
                        if (fill && fill.classList.contains('s-fill')) fill.style.width = pct + '%';
                    }

                    function buildCard(mod, idx) {
                        const el = document.createElement('div');
                        el.className = 'module-card' + (mod.enabled ? ' enabled' : '');
                        el.setAttribute('data-mod', mod.name);
                        el.style.animationDelay = (idx * 0.04) + 's';

                        const icon = ICONS[mod.name] || 'fa-microchip';
                        const bools  = mod.settings.filter(s => s.type === 'bool');
                        const others = mod.settings.filter(s => s.type !== 'bool');
                        const skipped = [];
                        let body = '';

                        others.forEach(s => {
                            if (skipped.includes(s.name)) return;
                            if (s.name.endsWith(' Min')) {
                                const base = s.name.replace(' Min','').trim();
                                const mx = mod.settings.find(x => x.name === base + ' Max');
                                if (mx) { skipped.push(s.name, mx.name); body += dualSlider(mod.name, base, s, mx); return; }
                            }
                             if (s.type === 'mode' || s.name.includes('Mode')) { body += modeRow(mod.name, s); return; }
                             if (s.type === 'keybind') { body += keybindRow(mod.name, s); return; }
                             if (s.type === 'multichoice') { body += multiChoiceRow(mod.name, s); return; }
                             body += sliderRow(mod.name, s);
                         });

                        if (bools.length) {
                            body += '<div class="bool-group">';
                            bools.forEach(s => { body += boolRow(mod.name, s); });
                            body += '</div>';
                        }

                        el.innerHTML = `
                            <div class="card-head">
                                <div class="card-left">
                                    <div class="card-icon"><i class="fa-solid ${icon}"></i></div>
                                    <span class="card-name">${mod.name}</span>
                                </div>
                                <div class="card-right">
                                    <button class="kb-btn" onclick="recordKey('${mod.name}',this)">${keyName(mod.key_bind)}</button>
                                    <label class="tog">
                                        <input type="checkbox" ${mod.enabled ? 'checked' : ''}
                                            onchange="toggleMod('${mod.name}',this.checked)">
                                        <div class="tog-track"><div class="tog-thumb"></div></div>
                                    </label>
                                </div>
                            </div>
                            ${body ? `<div class="card-divider"></div><div class="card-body">${body}</div>` : ''}
                        `;
                        return el;
                    }

                    function sliderRow(mod, s) {
                        const step = s.type === 'int' ? 1 : 0.1;
                        const pct  = ((s.value - s.min) / (s.max - s.min)) * 100;
                        return `
                            <div class="s-row" data-s="${s.name}">
                                <div class="s-head">
                                    <span class="s-name">${s.name}</span>
                                    <span class="s-val">${s.value}</span>
                                </div>
                                <div class="s-track">
                                    <div class="s-fill" style="width:${pct}%"></div>
                                    <input type="range" min="${s.min}" max="${s.max}" step="${step}" value="${s.value}"
                                        oninput="onSlider(this,'${mod}','${s.name}','${s.type}')"
                                        onchange="sendSetting('${mod}','${s.name}',this.value,'${s.type}')">
                                </div>
                            </div>`;
                    }

                    function boolRow(mod, s) {
                        return `
                            <div class="s-row bool${s.value?' on':''}" data-s="${s.name}">
                                <span class="s-name">${s.name}</span>
                                <label class="tog">
                                    <input type="checkbox" ${s.value?'checked':''} onchange="onBool(this,'${mod}','${s.name}')">
                                    <div class="tog-track"><div class="tog-thumb"></div></div>
                                </label>
                            </div>`;
                    }

                    function dualSlider(mod, base, mn, mx) {
                        const range = mn.max - mn.min;
                        const l = ((mn.value - mn.min) / range) * 100;
                        const r = ((mx.value - mn.min) / range) * 100;
                        return `
                            <div class="s-row" data-s="${mn.name}">
                                <div class="s-head">
                                    <span class="s-name">${base}</span>
                                    <span class="s-val">${mn.value} — ${mx.value}</span>
                                </div>
                                <div class="d-track">
                                    <div class="d-fill" style="left:${l}%;width:${r-l}%"></div>
                                    <div class="d-thumb" style="left:${l}%" onmousedown="startDrag(event,'${mod}','${mn.name}','${mx.name}',${mn.min},${mn.max},'min')"></div>
                                    <div class="d-thumb" style="left:${r}%" onmousedown="startDrag(event,'${mod}','${mn.name}','${mx.name}',${mn.min},${mn.max},'max')"></div>
                                </div>
                            </div>`;
                    }

                    function modeRow(mod, s) {
                        let btns = '';
                        if (s.modes) s.modes.forEach((name, i) => {
                            btns += `<div class="m-btn${s.value===i?' active':''}" onclick="selectMode(this,'${mod}','${s.name}',${i})">${name}</div>`;
                        });
                        return `
                            <div class="s-row" data-s="${s.name}">
                                <span class="s-name">${s.name.split('(')[0].trim()}</span>
                                <div class="m-row">${btns}</div>
                             </div>`;
                     }
 
                     function multiChoiceRow(mod, s) {
                         let opts = '';
                         s.options.forEach(o => {
                             const active = s.value.includes(o);
                             opts += `<div class="mc-opt${active?' active':''}" data-opt="${o}" onclick="onMulti(this,'${mod}','${s.name}','${o}')">
                                         <span>${o}</span><i class="fa-solid fa-check"></i>
                                      </div>`;
                         });
                         return `
                             <div class="s-row mc-row" data-s="${s.name}">
                                 <span class="s-name">${s.name}</span>
                                 <div class="mc-head" onclick="this.classList.toggle('open');this.nextElementSibling.classList.toggle('open')">
                                     <span class="s-val" style="font-size:0.72rem;color:var(--white);margin-right:8px">${s.value.length?s.value.join(', '):'None'}</span>
                                     <i class="fa-solid fa-chevron-down"></i>
                                 </div>
                                 <div class="mc-opts">${opts}</div>
                             </div>`;
                     }
 
                     function keybindRow(mod, s) {
                         return `
                             <div class="s-row s-kb-row" data-s="${s.name}">
                                 <span class="s-name">${s.name}</span>
                                 <button class="s-kb-btn" onclick="recordSettingKey('${mod}','${s.name}',this)">${keyName(s.value)}</button>
                             </div>`;
                     }

                    window.toggleMod = (name, val) => {
                        const m = config.find(x => x.name === name);
                        if (m) m.enabled = val;
                        const card = grid.querySelector(`.module-card[data-mod="${name}"]`);
                        if (card) card.classList.toggle('enabled', val);
                        send({ type:'toggle_module', module:name, enabled:val });
                    };

                    window.onSlider = (inp, mod, name, type) => {
                        fillSlider(inp);
                        const sv = inp.closest('.s-row').querySelector('.s-val');
                        const num = type==='int' ? parseInt(inp.value) : parseFloat(inp.value);
                        if (sv) sv.textContent = num;
                        const m = config.find(x => x.name === mod);
                        if (m) { const s = m.settings.find(x => x.name === name); if (s) s.value = num; }
                    };

                    window.sendSetting = (mod, name, val, type) => {
                        send({ type:'update_setting', module:mod, setting:name, value: type==='int' ? parseInt(val) : parseFloat(val) });
                    };

                    window.onBool = (inp, mod, name) => {
                        inp.closest('.s-row').classList.toggle('on', inp.checked);
                        const m = config.find(x => x.name === mod);
                        if (m) { const s = m.settings.find(x => x.name === name); if (s) s.value = inp.checked; }
                        send({ type:'update_setting', module:mod, setting:name, value:inp.checked });
                    };

                    window.selectMode = (btn, mod, name, idx) => {
                        btn.parentElement.querySelectorAll('.m-btn').forEach(b => b.classList.remove('active'));
                        btn.classList.add('active');
                        const m = config.find(x => x.name === mod);
                        if (m) { const s = m.settings.find(x => x.name === name); if (s) s.value = idx; }
                        send({ type:'update_setting', module:mod, setting:name, value:idx });
                    };

                    window.startDrag = (e, mod, minN, maxN, minL, maxL, which) => {
                        window.dragging = true;
                        e.preventDefault();
                        const track = e.target.parentElement;
                        const m = config.find(x => x.name === mod);
                        const minS = m.settings.find(s => s.name === minN);
                        const maxS = m.settings.find(s => s.name === maxN);

                        const move = me => {
                            const r = track.getBoundingClientRect();
                            let pct = Math.max(0, Math.min(1, (me.clientX - r.left) / r.width));
                            let val = minL + pct * (maxL - minL);
                            val = (maxL - minL > 20) ? Math.round(val) : Math.round(val * 10) / 10;
                            if (which==='min') minS.value = Math.min(val, maxS.value);
                            else               maxS.value = Math.max(val, minS.value);
                            const range = maxL - minL;
                            const lp = ((minS.value - minL) / range) * 100;
                            const rp = ((maxS.value - minL) / range) * 100;
                            track.querySelector('.d-fill').style.cssText = `left:${lp}%;width:${rp-lp}%`;
                            track.querySelectorAll('.d-thumb')[0].style.left = lp+'%';
                            track.querySelectorAll('.d-thumb')[1].style.left = rp+'%';
                            const sv = track.parentElement.querySelector('.s-val');
                            if (sv) sv.textContent = `${minS.value} — ${maxS.value}`;
                        };

                        const up = () => {
                            window.removeEventListener('mousemove', move);
                            window.removeEventListener('mouseup', up);
                            setTimeout(() => { window.dragging = false; }, 50);
                            send({ type:'update_setting', module:mod, setting:minN, value:minS.value });
                            send({ type:'update_setting', module:mod, setting:maxN, value:maxS.value });
                        };

                        window.addEventListener('mousemove', move);
                        window.addEventListener('mouseup', up);
                    };

                     window.recordKey = (mod, btn) => {
                         btn.textContent = '...';
                         btn.classList.add('recording');
                         const h = e => {
                             e.preventDefault();
                             let code = GLFW[e.code] || 0;
                             if (e.code === 'Escape' || e.code === 'Backspace') code = -1;
                             const m = config.find(x => x.name === mod);
                             if (m) m.key_bind = code;
                             send({ type:'update_keybind', module:mod, key:code });
                             btn.classList.remove('recording');
                             btn.textContent = keyName(code);
                             window.removeEventListener('keydown', h);
                         };
                         window.addEventListener('keydown', h);
                     };
 
                     window.recordSettingKey = (mod, name, btn) => {
                         btn.textContent = '...';
                         btn.classList.add('recording');
                         const h = e => {
                             e.preventDefault();
                             let code = GLFW[e.code] || 0;
                             if (e.code === 'Escape' || e.code === 'Backspace') code = -1;
                             const m = config.find(x => x.name === mod);
                             if (m) { const s = m.settings.find(x => x.name === name); if (s) s.value = code; }
                             send({ type:'update_setting', module:mod, setting:name, value:code });
                             btn.classList.remove('recording');
                             btn.textContent = keyName(code);
                             window.removeEventListener('keydown', h);
                         };
                         window.addEventListener('keydown', h);
                     };
 
                     window.onMulti = (opt, mod, name, val) => {
                         const m = config.find(x => x.name === mod);
                         const s = m.settings.find(x => x.name === name);
                         if (s.value.includes(val)) s.value = s.value.filter(x => x !== val);
                         else s.value.push(val);
                         
                         const row = opt.closest('.mc-row');
                         const head = row.querySelector('.mc-head .s-val');
                         head.textContent = s.value.length ? s.value.join(', ') : 'None';
                         opt.classList.toggle('active', s.value.includes(val));
                         
                         send({ type:'update_setting', module:mod, setting:name, value:s.value });
                     };

                    connect();
                </script>
            </body>
            </html>
            """;
}