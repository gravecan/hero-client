const fs = require('fs');
const path = require('path');
const { MessageFlags } = require('discord.js');

module.exports = (client) => {
    const interactionsPath = path.join(__dirname, '../interactions');
    const interactionFiles = fs.readdirSync(interactionsPath).filter(file => file.endsWith('.js'));

    for (const file of interactionFiles) {
        const interaction = require(path.join(interactionsPath, file));
        client.interactions.set(interaction.customId, interaction);
    }

    client.on('interactionCreate', async (interaction) => {
        if (interaction.isChatInputCommand()) {
            const command = client.commands.get(interaction.commandName);
            if (!command) return;

            try {
                await command.execute(interaction, client);
            } catch (error) {
                console.error(error);
                await interaction.reply({ content: 'There was an error while executing this command!', flags: MessageFlags.Ephemeral });
            }
        } else if (interaction.isStringSelectMenu() || interaction.isButton() || interaction.isModalSubmit()) {
            const handler = client.interactions.get(interaction.customId);
            if (handler) {
                try {
                    await handler.execute(interaction, client);
                } catch (error) {
                    console.error(error);
                    if (interaction.deferred || interaction.replied) {
                        await interaction.editReply({ content: 'There was an error while executing this interaction!' });
                    } else {
                        await interaction.reply({ content: 'There was an error while executing this interaction!', flags: MessageFlags.Ephemeral });
                    }
                }
            }
        }
    });
    console.log(`[Handler] Loaded ${interactionFiles.length} interactions.`);
};
