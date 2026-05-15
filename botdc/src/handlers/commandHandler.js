const { REST, Routes } = require('discord.js');
const fs = require('fs');
const path = require('path');

module.exports = (client) => {
    const commands = [];
    const commandsPath = path.join(__dirname, '../commands');
    const commandFiles = fs.readdirSync(commandsPath).filter(file => file.endsWith('.js'));

    for (const file of commandFiles) {
        const command = require(path.join(commandsPath, file));
        if ('data' in command && 'execute' in command) {
            client.commands.set(command.data.name, command);
            commands.push(command.data.toJSON());
        }
    }

    const rest = new REST({ version: '10' }).setToken(process.env.TOKEN);

    (async () => {
        try {
            console.log(`[Handler] Started refreshing ${commands.length} application (/) commands.`);

            const data = await rest.put(
                Routes.applicationGuildCommands(process.env.CLIENT_ID, process.env.GUILD_ID),
                { body: commands },
            );

            console.log(`[Handler] Successfully reloaded ${data.length} application (/) commands.`);
        } catch (error) {
            console.error(error);
        }
    })();
};
