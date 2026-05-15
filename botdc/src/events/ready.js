const { ActivityType, Events } = require('discord.js');

module.exports = {
    name: Events.ClientReady,
    once: true,
    execute(client) {
        console.log(`Logged in as ${client.user.tag}!`);
        console.log('Hero Client Bot is Online (Modular).');

        client.user.setActivity('Hero Client | Undetected', { type: ActivityType.Playing });
    },
};
