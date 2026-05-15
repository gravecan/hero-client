const { SlashCommandBuilder, EmbedBuilder, ButtonBuilder, ButtonStyle, ActionRowBuilder, ComponentType, MessageFlags, PermissionFlagsBits } = require('discord.js');
const config = require('../../config.json');

// Helper function to parse duration strings (e.g., "10m", "2h", "1d")
function parseDuration(str) {
    if (!str) return null;
    const match = str.match(/^(\d+)(s|m|h|d)$/);
    if (!match) return null;
    const value = parseInt(match[1]);
    const unit = match[2];
    switch (unit) {
        case 's': return value * 1000;
        case 'm': return value * 60 * 1000;
        case 'h': return value * 60 * 60 * 1000;
        case 'd': return value * 24 * 60 * 60 * 1000;
        default: return null;
    }
}

module.exports = {
    data: new SlashCommandBuilder()
        .setName('giveaway')
        .setDescription('Manage community giveaways')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
        .addSubcommand(subcommand =>
            subcommand
                .setName('start')
                .setDescription('Start a new giveaway')
                .addStringOption(option => option.setName('duration').setDescription('Duration (e.g. 10m, 1h, 1d)').setRequired(true))
                .addIntegerOption(option => option.setName('winners').setDescription('Number of winners').setRequired(true))
                .addStringOption(option => option.setName('prize').setDescription('Prize description').setRequired(true))),

    async execute(interaction) {
        // Role Check
        const hasRole = interaction.member.roles.cache.some(role => config.roles.admin.includes(role.id));
        if (!hasRole) {
            return interaction.reply({ content: '❌ You do not have permission to use this command.', flags: MessageFlags.Ephemeral });
        }

        if (interaction.options.getSubcommand() === 'start') {
            const duration = interaction.options.getString('duration');
            const winnerCount = interaction.options.getInteger('winners');
            const prize = interaction.options.getString('prize');

            const timeMs = parseDuration(duration);
            if (!timeMs) return interaction.reply({ content: '❌ Invalid duration format. Use format like `10m`, `1h`, `1d`.', flags: MessageFlags.Ephemeral });

            const endTime = Date.now() + timeMs;

            // Embed
            const giveawayEmbed = new EmbedBuilder()
                .setColor(config.colors.gold)
                .setTitle('🎉  GIVEAWAY  🎉')
                .setDescription(`
**Prize:** ${prize}
**Winners:** ${winnerCount}
**Ends:** <t:${Math.floor(endTime / 1000)}:R> (<t:${Math.floor(endTime / 1000)}:f>)

*Click the button below to join!*
                `)
                .setTimestamp(endTime)
                .setFooter({ text: 'Hero Client • Community Events' });

            // Button
            const joinButton = new ButtonBuilder()
                .setCustomId('join_giveaway')
                .setLabel('🎉 Join Giveaway')
                .setStyle(ButtonStyle.Success);

            const row = new ActionRowBuilder().addComponents(joinButton);

            const message = await interaction.channel.send({ embeds: [giveawayEmbed], components: [row] });
            await interaction.reply({ content: `✅ Giveaway started! Ends <t:${Math.floor(endTime / 1000)}:R>`, flags: MessageFlags.Ephemeral });

            // Collector
            const participants = new Set();
            const filter = i => i.customId === 'join_giveaway';
            const collector = message.createMessageComponentCollector({ filter, componentType: ComponentType.Button, time: timeMs });

            collector.on('collect', async i => {
                if (participants.has(i.user.id)) {
                    await i.reply({ content: '⚠️ You have already joined this giveaway.', flags: MessageFlags.Ephemeral });
                } else {
                    participants.add(i.user.id);
                    await i.reply({ content: '✅ Entry confirmed! Good luck.', flags: MessageFlags.Ephemeral });
                }
            });

            collector.on('end', async () => {
                const participantsArray = Array.from(participants);
                const disabledRow = new ActionRowBuilder().addComponents(joinButton.setDisabled(true));

                if (participantsArray.length < winnerCount) {
                    message.edit({ components: [disabledRow] });
                    return message.channel.send(`❌ Giveaway ended. Not enough participants for **${prize}**.`);
                }

                // Select winners
                const winners = [];
                for (let i = 0; i < winnerCount; i++) {
                    const randomIndex = Math.floor(Math.random() * participantsArray.length);
                    winners.push(participantsArray[randomIndex]);
                    participantsArray.splice(randomIndex, 1);
                }

                const winnerMentions = winners.map(id => `<@${id}>`).join(', ');

                const endEmbed = new EmbedBuilder()
                    .setColor(config.colors.gold)
                    .setTitle('🎉  GIVEAWAY ENDED  🎉')
                    .setDescription(`
**Prize:** ${prize}
**Winners:** ${winnerMentions}

*Congratulations! Please open a support ticket to claim your reward.*
                    `)
                    .setTimestamp()
                    .setFooter({ text: `Ended • ${participants.size} Entries` });

                await message.edit({ embeds: [endEmbed], components: [disabledRow] });
                await message.channel.send(`🎉 Congratulations ${winnerMentions}! You won **${prize}**!`);

                // DM Winners
                for (const winnerId of winners) {
                    try {
                        const user = await interaction.client.users.fetch(winnerId);
                        const dmEmbed = new EmbedBuilder()
                            .setColor(config.colors.gold)
                            .setTitle('🏆 You Won a Giveaway!')
                            .setDescription(`
**Prize:** ${prize}
**Server:** ${interaction.guild.name}

Please open a **Support Ticket** to claim your reward.
                            `)
                            .setFooter({ text: 'Hero Client • Winner Notification' });

                        await user.send({ embeds: [dmEmbed] });
                    } catch (error) {
                        console.error(`Could not DM winner ${winnerId}:`, error);
                    }
                }
            });
        }
    },
};
