const { EmbedBuilder, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    customId: 'close_ticket',
    async execute(interaction) {
        const member = interaction.member;

        // 1. Role Restriction: Only Support can close
        if (!member.roles.cache.has(config.roles.support)) {
            return interaction.reply({ content: '❌ Only **Support Staff** can close tickets.', flags: MessageFlags.Ephemeral });
        }

        await interaction.deferReply();

        await interaction.editReply({
            embeds: [
                new EmbedBuilder()
                    .setColor(config.colors.error)
                    .setTitle('🔒 DISCONNECTING')
                    .setDescription('This ticket is being archived and closed. A transcript will be sent to the logs.')
                    .setFooter({ text: 'Channel deletion in 5 seconds...' })
            ]
        });

        // 2. Transcript System
        try {
            const messages = await interaction.channel.messages.fetch({ limit: 100 });
            let transcript = `HERO CLIENT TICKET TRANSCRIPT\n`;
            transcript += `Channel: ${interaction.channel.name}\n`;
            transcript += `Closed by: ${interaction.user.tag}\n`;
            transcript += `----------------------------------------\n\n`;

            const log = messages.reverse().map(m => `[${m.createdAt.toLocaleString()}] ${m.author.tag}: ${m.content}`).join('\n');
            transcript += log;

            const transcriptChannel = interaction.guild.channels.cache.get(config.channels.transcripts);
            if (transcriptChannel) {
                const logEmbed = new EmbedBuilder()
                    .setColor(config.colors.gold)
                    .setTitle('📄 Ticket Transcript')
                    .addFields(
                        { name: 'Channel', value: `\`${interaction.channel.name}\``, inline: true },
                        { name: 'Closed By', value: `${interaction.user}`, inline: true }
                    )
                    .setTimestamp();

                // Send as a file attachment
                await transcriptChannel.send({
                    embeds: [logEmbed],
                    files: [{ attachment: Buffer.from(transcript), name: `transcript-${interaction.channel.name}.txt` }]
                });
            }
        } catch (err) {
            console.error('Transcript error:', err);
        }

        setTimeout(() => interaction.channel.delete().catch(() => { }), 5000);
    },
};
