const WebSocket = require('ws');

const PORT = 8080;
const wss = new WebSocket.Server({ port: PORT });
const clients = new Map(); // user -> { game: ws, dashboards: [ws] }

console.log(`Relay Server running on port ${PORT}`);

wss.on('connection', (ws) => {
    let userData = null;

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);

            // 1. Identification
            if (data.type === 'identify') {
                const user = data.user;
                const role = data.role; // 'client' or 'dashboard'

                if (!user || !role) return;

                userData = { user, role };

                if (!clients.has(user)) {
                    clients.set(user, { game: null, dashboards: [] });
                }

                const session = clients.get(user);

                if (role === 'client') {
                    if (session.game) session.game.close(); // Close old game connection
                    session.game = ws;
                    console.log(`[GAME] User ${user} connected.`);

                    // Request state immediately so inconsistent dashboards get updated
                    ws.send(JSON.stringify({ type: 'get_state' }));
                } else if (role === 'dashboard') {
                    session.dashboards.push(ws);
                    console.log(`[DASH] User ${user} dashboard connected.`);

                    // If game is online, request state immediately
                    if (session.game && session.game.readyState === WebSocket.OPEN) {
                        session.game.send(JSON.stringify({ type: 'get_state' }));
                    }
                }
                return;
            }

            // 2. Routing
            if (!userData) return; // Not identified yet

            const session = clients.get(userData.user);
            if (!session) return;

            if (userData.role === 'client') {
                // Game -> Dashboards (Broadcast)
                session.dashboards.forEach(dash => {
                    if (dash.readyState === WebSocket.OPEN) {
                        dash.send(message);
                    }
                });
            } else if (userData.role === 'dashboard') {
                // Dashboard -> Game (Direct)
                if (session.game && session.game.readyState === WebSocket.OPEN) {
                    session.game.send(message);
                }
            }

        } catch (e) {
            console.error('Error parsing message:', e);
        }
    });

    ws.on('close', () => {
        if (!userData) return;
        const session = clients.get(userData.user);
        if (!session) return;

        if (userData.role === 'client') {
            if (session.game === ws) {
                session.game = null;
                console.log(`[GAME] User ${userData.user} disconnected.`);
            }
        } else if (userData.role === 'dashboard') {
            session.dashboards = session.dashboards.filter(dash => dash !== ws);
            console.log(`[DASH] User ${userData.user} dashboard disconnected.`);
        }

        // Cleanup empty sessions
        if (!session.game && session.dashboards.length === 0) {
            clients.delete(userData.user);
        }
    });

    ws.on('error', (err) => {
        console.error('WS Error:', err.message);
    });
});
