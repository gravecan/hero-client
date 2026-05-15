const { EmbedBuilder, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    customId: 'ping_user',
    async execute(interaction) {
        const member = interaction.member;

        // 1. Role Restriction: Only Support can ping
        if (!member.roles.cache.has(config.roles.support)) {
            return interaction.reply({ content: '❌ Only **Support Staff** can nudge users.', flags: MessageFlags.Ephemeral });
        }

        // Get the user from the channel name or mentions (assuming ticket owner)
        // Since we restricted channel access to the owner, they are the only non-staff there usually.
        // But a better way is to find the user in permission overwrites or by name.
        const ticketOwnerId = interaction.channel.permissionOverwrites.cache
            .find(o => o.type === 1 && o.id !== interaction.guild.id && o.id !== config.roles.support)?.id;

        if (!ticketOwnerId) {
            return interaction.reply({ content: '❌ Could not identify the ticket owner.', flags: MessageFlags.Ephemeral });
        }

        await interaction.deferReply({ flags: MessageFlags.Ephemeral });

        try {
            const user = await interaction.client.users.fetch(ticketOwnerId);
            const pingEmbed = new EmbedBuilder()
                .setColor(config.colors.gold)
                .setTitle('💎 Hero Client | Active Ticket')
                .setDescription(`Hello ${user},\n\nour support staff is waiting for your response in your ticket: ${interaction.channel}.\n\nPlease head over to the channel so we can assist you further.`)
                .setFooter({ text: 'This is an automated notification.' });

            await user.send({ embeds: [pingEmbed] });
            await interaction.editReply({ content: `✅ Sent a DM reminder to ${user.tag}.` });
        } catch (err) {
            console.error('Ping DM error:', err);
            await interaction.editReply({ content: '❌ Could not send DM to the user (DMs might be closed).' });
        }
    },
};
