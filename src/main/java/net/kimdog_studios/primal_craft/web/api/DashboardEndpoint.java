package net.kimdog_studios.primal_craft.web.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.kimdog_studios.primal_craft.web.server.WebServer;

import java.io.IOException;

/**
 * Serves the dashboard HTML/CSS/JS interface
 */
public class DashboardEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("/".equals(exchange.getRequestURI().getPath()) || "".equals(exchange.getRequestURI().getPath())) {
            WebServer.sendHTML(exchange, getDashboardHTML());
        } else {
            WebServer.sendError(exchange, 404, "Not found");
        }
    }

    private String getDashboardHTML() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Primal Craft Dashboard</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
            color: #333;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }

        header h1 {
            font-size: 3em;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        header p {
            font-size: 1.2em;
            opacity: 0.9;
        }

        .status-bar {
            background: white;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
        }

        .status-item {
            text-align: center;
        }

        .status-label {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 5px;
        }

        .status-value {
            font-size: 2em;
            font-weight: bold;
            color: #667eea;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
            gap: 20px;
            margin-bottom: 20px;
        }

        .card {
            background: white;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            transition: transform 0.3s, box-shadow 0.3s;
        }

        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 12px rgba(0,0,0,0.2);
        }

        .card h2 {
            color: #667eea;
            margin-bottom: 20px;
            font-size: 1.5em;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }

        .setting-group {
            margin-bottom: 20px;
        }

        .setting-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }

        .setting-row:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }

        .setting-label {
            font-weight: 500;
            color: #333;
            flex: 1;
        }

        .toggle {
            width: 50px;
            height: 26px;
            background: #ccc;
            border-radius: 13px;
            cursor: pointer;
            position: relative;
            transition: background 0.3s;
            border: none;
        }

        .toggle.active {
            background: #667eea;
        }

        .toggle::before {
            content: '';
            position: absolute;
            width: 22px;
            height: 22px;
            background: white;
            border-radius: 50%;
            top: 2px;
            left: 2px;
            transition: left 0.3s;
        }

        .toggle.active::before {
            left: 26px;
        }

        .slider-container {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        input[type="range"] {
            flex: 1;
            height: 6px;
            border-radius: 3px;
            background: #ddd;
            outline: none;
            -webkit-appearance: none;
        }

        input[type="range"]::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
        }

        input[type="range"]::-moz-range-thumb {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
            border: none;
        }

        .value-display {
            min-width: 50px;
            text-align: right;
            font-weight: 600;
            color: #667eea;
        }

        .chat-box {
            display: flex;
            flex-direction: column;
            gap: 10px;
            height: 400px;
        }

        .chat-messages {
            flex: 1;
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
            overflow-y: auto;
            background: #f9f9f9;
        }

        .chat-message {
            padding: 8px;
            margin-bottom: 8px;
            background: white;
            border-left: 3px solid #667eea;
            border-radius: 3px;
            font-size: 0.95em;
        }

        .chat-timestamp {
            font-size: 0.8em;
            color: #999;
            margin-right: 5px;
        }

        .chat-input {
            display: flex;
            gap: 10px;
        }

        input[type="text"] {
            flex: 1;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 1em;
        }

        button {
            padding: 10px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: background 0.3s;
        }

        button:hover {
            background: #764ba2;
        }

        button:active {
            transform: scale(0.98);
        }

        .players-list {
            max-height: 300px;
            overflow-y: auto;
        }

        .player-item {
            padding: 10px;
            background: #f9f9f9;
            border-radius: 5px;
            margin-bottom: 8px;
            border-left: 3px solid #667eea;
        }

        .player-name {
            font-weight: 600;
            color: #333;
        }

        .player-stats {
            font-size: 0.9em;
            color: #666;
            margin-top: 5px;
        }

        .stat-bar {
            display: flex;
            gap: 5px;
            margin-top: 5px;
            font-size: 0.85em;
        }

        .stat-bar-item {
            flex: 1;
            height: 20px;
            background: #ddd;
            border-radius: 3px;
            overflow: hidden;
        }

        .stat-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #667eea, #764ba2);
            transition: width 0.3s;
        }

        .alert {
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 15px;
            display: none;
        }

        .alert.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
            display: block;
        }

        .alert.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            display: block;
        }

        .footer {
            text-align: center;
            color: white;
            margin-top: 30px;
            opacity: 0.8;
        }

        @media (max-width: 768px) {
            .grid {
                grid-template-columns: 1fr;
            }

            header h1 {
                font-size: 2em;
            }

            .status-bar {
                grid-template-columns: repeat(2, 1fr);
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>⚡ Primal Craft Dashboard</h1>
            <p>Manage your game settings in real-time</p>
        </header>

        <div id="alert" class="alert"></div>

        <div class="status-bar">
            <div class="status-item">
                <div class="status-label">Server Status</div>
                <div class="status-value" id="serverStatus">🔴 Offline</div>
            </div>
            <div class="status-item">
                <div class="status-label">Players Online</div>
                <div class="status-value" id="playerCount">0/20</div>
            </div>
            <div class="status-item">
                <div class="status-label">Server Ticks</div>
                <div class="status-value" id="serverTicks">0</div>
            </div>
        </div>

        <div class="grid">
            <!-- Gameplay Settings -->
            <div class="card">
                <h2>🎮 Gameplay</h2>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">Stamina System</span>
                        <button class="toggle active" data-setting="gameplay.staminaSystemEnabled" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Stamina Depletion</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="gameplay.staminaDepletionRate" onchange="updateSetting(this)">
                            <span class="value-display" id="val-staminaDepletionRate">1.0x</span>
                        </div>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Stamina Recovery</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="gameplay.staminaRecoveryRate" onchange="updateSetting(this)">
                            <span class="value-display" id="val-staminaRecoveryRate">1.0x</span>
                        </div>
                    </div>
                </div>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">Thirst System</span>
                        <button class="toggle active" data-setting="gameplay.thirstSystemEnabled" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Thirst Depletion</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="gameplay.thirstDepletionRate" onchange="updateSetting(this)">
                            <span class="value-display" id="val-thirstDepletionRate">1.0x</span>
                        </div>
                    </div>
                </div>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">Temperature System</span>
                        <button class="toggle active" data-setting="gameplay.temperatureSystemEnabled" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Environmental Hazards</span>
                        <button class="toggle active" data-setting="gameplay.environmentalHazardsEnabled" onclick="toggleSetting(this)"></button>
                    </div>
                </div>
            </div>

            <!-- HUD Settings -->
            <div class="card">
                <h2>👁️ HUD Settings</h2>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">Show Stamina Bar</span>
                        <button class="toggle active" data-setting="hud.showStaminaBar" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Show Thirst Bar</span>
                        <button class="toggle active" data-setting="hud.showThirstBar" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Show Temperature</span>
                        <button class="toggle active" data-setting="hud.showTemperatureIndicator" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Show Weather Notifications</span>
                        <button class="toggle active" data-setting="hud.showWeatherNotifications" onclick="toggleSetting(this)"></button>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Show Biome Notifications</span>
                        <button class="toggle active" data-setting="hud.showBiomeNotifications" onclick="toggleSetting(this)"></button>
                    </div>
                </div>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">HUD Scale</span>
                        <div class="slider-container">
                            <input type="range" min="0.5" max="2" step="0.1" value="1" data-setting="hud.hudScale" onchange="updateSetting(this)">
                            <span class="value-display" id="val-hudScale">1.0x</span>
                        </div>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">HUD Opacity</span>
                        <div class="slider-container">
                            <input type="range" min="0" max="1" step="0.1" value="1" data-setting="hud.hudOpacity" onchange="updateSetting(this)">
                            <span class="value-display" id="val-hudOpacity">100%</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Difficulty Settings -->
            <div class="card">
                <h2>⚔️ Difficulty</h2>
                <div class="setting-group">
                    <div class="setting-row">
                        <span class="setting-label">Stamina Loss</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="difficulty.staminalossDifficulty" onchange="updateSetting(this)">
                            <span class="value-display" id="val-staminalossDifficulty">1.0x</span>
                        </div>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Thirst Difficulty</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="difficulty.thirstDifficulty" onchange="updateSetting(this)">
                            <span class="value-display" id="val-thirstDifficulty">1.0x</span>
                        </div>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Temperature</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="difficulty.temperatureDifficulty" onchange="updateSetting(this)">
                            <span class="value-display" id="val-temperatureDifficulty">1.0x</span>
                        </div>
                    </div>
                    <div class="setting-row">
                        <span class="setting-label">Hazard Difficulty</span>
                        <div class="slider-container">
                            <input type="range" min="0.1" max="3" step="0.1" value="1" data-setting="difficulty.hazardDifficulty" onchange="updateSetting(this)">
                            <span class="value-display" id="val-hazardDifficulty">1.0x</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Chat -->
            <div class="card">
                <h2>💬 Send Message</h2>
                <div class="chat-box">
                    <div class="chat-messages" id="chatMessages">
                        <div class="chat-message">
                            <span class="chat-timestamp">Ready to send messages!</span>
                        </div>
                    </div>
                    <div class="chat-input">
                        <input type="text" id="chatInput" placeholder="Type a message...">
                        <button onclick="sendChat()">Send</button>
                    </div>
                </div>
            </div>

            <!-- Players -->
            <div class="card">
                <h2>👥 Online Players</h2>
                <div class="players-list" id="playersList">
                    <p style="color: #999;">No players online</p>
                </div>
            </div>
        </div>

        <div class="footer">
            <p>Primal Craft Dashboard • © KimDog Studios</p>
        </div>
    </div>

    <script>
        const API_BASE = 'http://localhost:8888/api';

        // Load initial config
        loadConfig();
        loadStatus();

        // Refresh status every 2 seconds
        setInterval(loadStatus, 2000);

        // Chat input enter key
        document.getElementById('chatInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendChat();
        });

        function showAlert(message, type) {
            const alert = document.getElementById('alert');
            alert.textContent = message;
            alert.className = `alert ${type}`;
            setTimeout(() => {
                alert.className = 'alert';
            }, 3000);
        }

        function loadConfig() {
            fetch(`${API_BASE}/config`)
                .then(r => r.json())
                .then(data => {
                    // Load gameplay settings
                    setToggle('gameplay.staminaSystemEnabled', data.gameplay.staminaSystemEnabled);
                    setSlider('gameplay.staminaDepletionRate', data.gameplay.staminaDepletionRate);
                    setSlider('gameplay.staminaRecoveryRate', data.gameplay.staminaRecoveryRate);
                    setToggle('gameplay.thirstSystemEnabled', data.gameplay.thirstSystemEnabled);
                    setSlider('gameplay.thirstDepletionRate', data.gameplay.thirstDepletionRate);
                    setToggle('gameplay.temperatureSystemEnabled', data.gameplay.temperatureSystemEnabled);
                    setToggle('gameplay.environmentalHazardsEnabled', data.gameplay.environmentalHazardsEnabled);

                    // Load HUD settings
                    setToggle('hud.showStaminaBar', data.hud.showStaminaBar);
                    setToggle('hud.showThirstBar', data.hud.showThirstBar);
                    setToggle('hud.showTemperatureIndicator', data.hud.showTemperatureIndicator);
                    setToggle('hud.showWeatherNotifications', data.hud.showWeatherNotifications);
                    setToggle('hud.showBiomeNotifications', data.hud.showBiomeNotifications);
                    setSlider('hud.hudScale', data.hud.hudScale);
                    setSlider('hud.hudOpacity', data.hud.hudOpacity);

                    // Load difficulty settings
                    setSlider('difficulty.staminalossDifficulty', data.difficulty.staminalossDifficulty);
                    setSlider('difficulty.thirstDifficulty', data.difficulty.thirstDifficulty);
                    setSlider('difficulty.temperatureDifficulty', data.difficulty.temperatureDifficulty);
                    setSlider('difficulty.hazardDifficulty', data.difficulty.hazardDifficulty);
                })
                .catch(e => console.error('Failed to load config:', e));
        }

        function loadStatus() {
            fetch(`${API_BASE}/status`)
                .then(r => r.json())
                .then(data => {
                    if (data.serverRunning) {
                        document.getElementById('serverStatus').textContent = '🟢 Online';
                        document.getElementById('playerCount').textContent = `${data.playerCount}/${data.maxPlayers}`;
                        document.getElementById('serverTicks').textContent = data.ticks;

                        // Update players list
                        const playersList = document.getElementById('playersList');
                        if (data.players.length > 0) {
                            playersList.innerHTML = data.players.map(p => `
                                <div class="player-item">
                                    <div class="player-name">${p.name}</div>
                                    <div class="player-stats">
                                        Level ${p.level} • Food ${p.food}
                                    </div>
                                    <div class="stat-bar">
                                        <div style="flex: 1; color: #999; font-size: 0.8em;">Health</div>
                                        <div class="stat-bar-item" style="flex: 2;">
                                            <div class="stat-bar-fill" style="width: ${(p.health / p.maxHealth) * 100}%"></div>
                                        </div>
                                    </div>
                                </div>
                            `).join('');
                        } else {
                            playersList.innerHTML = '<p style="color: #999;">No players online</p>';
                        }
                    } else {
                        document.getElementById('serverStatus').textContent = '🔴 Offline';
                    }
                })
                .catch(e => console.error('Failed to load status:', e));
        }

        function setToggle(setting, value) {
            const btn = document.querySelector(`button[data-setting="${setting}"]`);
            if (btn) {
                if (value) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            }
        }

        function setSlider(setting, value) {
            const input = document.querySelector(`input[data-setting="${setting}"]`);
            const display = document.getElementById(`val-${setting.split('.')[1]}`);
            if (input) {
                input.value = value;
                if (display) {
                    if (input.type === 'range' && input.max === '1') {
                        display.textContent = Math.round(value * 100) + '%';
                    } else {
                        display.textContent = value.toFixed(1) + 'x';
                    }
                }
            }
        }

        function toggleSetting(btn) {
            const setting = btn.getAttribute('data-setting');
            const [category, key] = setting.split('.');
            const value = btn.classList.toggle('active');

            const payload = {};
            payload[category] = {};
            payload[category][key] = value;

            fetch(`${API_BASE}/config`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    showAlert(`✅ ${key} updated`, 'success');
                } else {
                    showAlert('❌ Failed to update setting', 'error');
                }
            })
            .catch(e => {
                showAlert('❌ Connection error', 'error');
                btn.classList.toggle('active');
            });
        }

        function updateSetting(input) {
            const setting = input.getAttribute('data-setting');
            const [category, key] = setting.split('.');
            const value = parseFloat(input.value);

            const display = document.getElementById(`val-${key}`);
            if (display) {
                if (input.max === '1') {
                    display.textContent = Math.round(value * 100) + '%';
                } else {
                    display.textContent = value.toFixed(1) + 'x';
                }
            }

            const payload = {};
            payload[category] = {};
            payload[category][key] = value;

            fetch(`${API_BASE}/config`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    showAlert(`✅ ${key} updated to ${value.toFixed(1)}x`, 'success');
                }
            })
            .catch(e => showAlert('❌ Connection error', 'error'));
        }

        function sendChat() {
            const input = document.getElementById('chatInput');
            const message = input.value.trim();

            if (!message) return;

            fetch(`${API_BASE}/chat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    message: message,
                    sender: 'Dashboard'
                })
            })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    const chatMessages = document.getElementById('chatMessages');
                    const msg = document.createElement('div');
                    msg.className = 'chat-message';
                    msg.innerHTML = `<span class="chat-timestamp">${new Date().toLocaleTimeString()}</span> [Dashboard] ${message}`;
                    chatMessages.appendChild(msg);
                    chatMessages.scrollTop = chatMessages.scrollHeight;
                    input.value = '';
                    showAlert('✅ Message sent', 'success');
                }
            })
            .catch(e => showAlert('❌ Failed to send message', 'error'));
        }
    </script>
</body>
</html>
""";
    }
}
