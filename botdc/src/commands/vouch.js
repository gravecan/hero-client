const { SlashCommandBuilder, ModalBuilder, TextInputBuilder, TextInputStyle, ActionRowBuilder, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    data: new SlashCommandBuilder()
        .setName('vouch')
        .setDescription('Leave a review about your experience with Hero Client'),

    async execute(interaction) {
        // Role Restriction: Only clients can vouch
        if (!interaction.member.roles.cache.has(config.roles.client)) {
            return interaction.reply({
                content: '❌ This command is reserved for **Verified Clients** only.',
                flags: MessageFlags.Ephemeral
            });
        }

        const modal = new ModalBuilder()
            .setCustomId('vouch_modal')
            .setTitle('Hero Client | Review Protocol');

        const starInput = new TextInputBuilder()
            .setCustomId('vouch_stars')
            .setLabel('Rating (1-5)')
            .setPlaceholder('Enter a number from 1 to 5...')
            .setStyle(TextInputStyle.Short)
            .setMinLength(1)
            .setMaxLength(1)
            .setRequired(true);

        const commentInput = new TextInputBuilder()
            .setCustomId('vouch_comment')
            .setLabel('Your Feedback')
            .setPlaceholder('Tell us about your experience...')
            .setStyle(TextInputStyle.Paragraph)
            .setMinLength(5)
            .setMaxLength(1000)
            .setRequired(true);

        const firstActionRow = new ActionRowBuilder().addComponents(starInput);
        const secondActionRow = new ActionRowBuilder().addComponents(commentInput);

        modal.addComponents(firstActionRow, secondActionRow);

        await interaction.showModal(modal);
    },
};
