const { EmbedBuilder, AuditLogEvent } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    name: 'messageDelete',
    async execute(message) {
        // 1. Ignore if message is partial (content unknown) or bot
        if (message.partial) return;
        if (message.author.bot) return;

        // 2. Check if it's a ticket channel
        if (!message.channel.name.startsWith('🎫-')) return;

        // 3. Try to fetch audit logs to see WHO deleted it
        let executor = null;
        try {
            const fetchedLogs = await message.guild.fetchAuditLogs({
                limit: 1,
                type: AuditLogEvent.MessageDelete,
            });
            const deletionLog = fetchedLogs.entries.first();

            // Check if the log is relevant (created recently and targets the message author)
            // Note: Audit logs are not perfect for matching exact messages.
            if (deletionLog && deletionLog.target.id === message.author.id && deletionLog.createdTimestamp > (Date.now() - 5000)) {
                executor = deletionLog.executor;
            }
        } catch (error) {
            console.error('Error fetching audit logs:', error);
        }

        let deletedBy = `${message.author.tag} (Self)`;
        if (executor) {
            deletedBy = `${executor.tag} (Staff)`;
        }

        const logEmbed = new EmbedBuilder()
            .setColor(config.colors.gold)
            .setAuthor({ name: '🗑️ Message Deleted' })
            .setDescription(`
**Author:** <@${message.author.id}>
**Deleted By:** \`${deletedBy}\`

**Content:**
\`\`\`
${message.content || '[No Text Content]'}
\`\`\`
            `)
            .setFooter({ text: `User ID: ${message.author.id}` })
            .setTimestamp();

        // Check for attachments
        if (message.attachments.size > 0) {
            logEmbed.addFields({ name: '📎 Attachment', value: '> *Media was attached*' });
        }

        await message.channel.send({ embeds: [logEmbed] });
    },
};
