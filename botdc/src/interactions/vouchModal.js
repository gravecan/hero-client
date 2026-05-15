const { EmbedBuilder, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    customId: 'vouch_modal',
    async execute(interaction, client) {
        const starInput = interaction.fields.getTextInputValue('vouch_stars');
        const comment = interaction.fields.getTextInputValue('vouch_comment');

        const stars = parseInt(starInput);

        // Validation for numeric input
        if (isNaN(stars) || stars < 1 || stars > 5) {
            return interaction.reply({
                content: '❌ Invalid rating. Please enter a number between **1** and **5**.',
                flags: MessageFlags.Ephemeral
            });
        }

        const reviewChannelId = config.channels.reviews;
        const reviewChannel = interaction.guild.channels.cache.get(reviewChannelId);

        if (!reviewChannel) {
            return interaction.reply({
                content: '❌ Review channel not found. Please check configuration.',
                flags: MessageFlags.Ephemeral
            });
        }

        const starDisplay = '⭐'.repeat(stars);

        const vouchEmbed = new EmbedBuilder()
            .setColor(config.colors.gold)
            .setAuthor({ name: '📣  New Feedback' })
            .setDescription(`
\`\`\`
${comment}
\`\`\`

**Rating:** ${starDisplay}
            `)
            .setTimestamp()
            .setFooter({ text: `User ID: ${interaction.user.id}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true }) });

        await reviewChannel.send({ embeds: [vouchEmbed] });

        await interaction.reply({
            content: `✅ Thank you for your review! It has been posted in ${reviewChannel}.`,
            flags: MessageFlags.Ephemeral
        });
    },
};
