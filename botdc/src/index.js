const { Client, GatewayIntentBits, Partials, Collection } = require('discord.js');
require('dotenv').config();
const fs = require('fs');
const path = require('path');

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.GuildMembers,
    ],
    partials: [Partials.Channel, Partials.Message],
});

// Collections for handlers
client.commands = new Collection();
client.interactions = new Collection();

// Load Handlers
const handlersPath = path.join(__dirname, 'handlers');
fs.readdirSync(handlersPath).forEach(handlerFile => {
    require(path.join(handlersPath, handlerFile))(client);
});

client.login(process.env.TOKEN);
