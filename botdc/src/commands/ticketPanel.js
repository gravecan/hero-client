const { SlashCommandBuilder, EmbedBuilder, ActionRowBuilder, StringSelectMenuBuilder, PermissionFlagsBits, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    data: new SlashCommandBuilder()
        .setName('ticket-panel')
        .setDescription('Deploy the premium ticket panel')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),
    async execute(interaction) {
        // Role Check
        const hasRole = interaction.member.roles.cache.some(role => config.roles.admin.includes(role.id));
        if (!hasRole) {
            return interaction.reply({ content: '❌ You do not have permission to use this command.', flags: MessageFlags.Ephemeral });
        }

        const row = new ActionRowBuilder()
            .addComponents(
                new StringSelectMenuBuilder()
                    .setCustomId('ticket_select')
                    .setPlaceholder('Select a category to open a ticket')
                    .addOptions([
                        { label: 'Buy Hero', description: 'Purchase a license', value: 'buy', emoji: '💰' },
                        { label: 'Support', description: 'Technical assistance', value: 'support', emoji: '🛠️' },
                        { label: 'Reset HWID', description: 'Request hardware reset', value: 'resethwid', emoji: '🔄' },
                    ]),
            );

        const embed = new EmbedBuilder()
            .setColor(config.colors.gold)
            .setAuthor({ name: 'HERO CLIENT', iconURL: interaction.guild.iconURL() })
            .setTitle('Official Support Hub')
            .setDescription('**Welcome to the Hero Client Support Terminal.**\nTo initiate a secure session with our staff, please select the appropriate department from the menu below.')
            .addFields(
                { name: '💸 | Sales & Licensing', value: '> *Inquiries regarding acquisition and billing.*', inline: false },
                { name: '⚙️ | Technical Support', value: '> *Assistance with setup and software functionality.*', inline: false },
                { name: '🔄 | HWID Management', value: '> *Requests for hardware identification resets.*', inline: false },
                { name: '💎 | Service Information', value: '```yaml\nAll Systems Operational • Follow Community Guidelines\n```' }
            )
            .setFooter({ text: 'Hero Client • Gold Standard Support' });

        await interaction.reply({ content: 'Ticket panel deployed!', flags: MessageFlags.Ephemeral });
        await interaction.channel.send({ embeds: [embed], components: [row] });
    },
};

