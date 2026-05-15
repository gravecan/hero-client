const { SlashCommandBuilder, EmbedBuilder, ChannelType, PermissionFlagsBits, MessageFlags } = require('discord.js');
const config = require('../../config.json');

const announceTypes = {
    'update': { color: '#2ecc71', emoji: '📢', title: 'UPDATE DEPLOYED' },
    'maintenance': { color: '#e67e22', emoji: '🛠️', title: 'SYSTEM MAINTENANCE' },
    'alert': { color: '#e74c3c', emoji: '🚨', title: 'CRITICAL ALERT' },
    'promotion': { color: config.colors.gold, emoji: '💎', title: 'LIMITED PROMOTION' },
    'general': { color: '#3498db', emoji: 'ℹ️', title: 'ANNOUNCEMENT' }
};

module.exports = {
    data: new SlashCommandBuilder()
        .setName('announce')
        .setDescription('Send a global announcement')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
        .addChannelOption(option =>
            option.setName('channel')
                .setDescription('Channel to send the announcement')
                .addChannelTypes(ChannelType.GuildText, ChannelType.GuildAnnouncement)
                .setRequired(true))
        .addStringOption(option =>
            option.setName('type')
                .setDescription('Type of announcement')
                .setRequired(true)
                .addChoices(
                    { name: '📢 Update', value: 'update' },
                    { name: '🛠️ Maintenance', value: 'maintenance' },
                    { name: '🚨 Critical Alert', value: 'alert' },
                    { name: '💎 Promotion', value: 'promotion' },
                    { name: 'ℹ️ General', value: 'general' }
                ))
        .addStringOption(option =>
            option.setName('message')
                .setDescription('Announcement Message (supports \\n for new lines)')
                .setRequired(true))
        .addStringOption(option =>
            option.setName('ping')
                .setDescription('Who to mention?')
                .addChoices(
                    { name: '🔕 None', value: 'none' },
                    { name: '📢 @everyone', value: 'everyone' },
                    { name: '🔔 @here', value: 'here' },
                    { name: '👥 Clients (@Client)', value: 'client' }
                ))
        .addAttachmentOption(option =>
            option.setName('image')
                .setDescription('Optional banner image'))
        .addStringOption(option =>
            option.setName('footer')
                .setDescription('Custom footer text')),

    async execute(interaction) {
        // Role Check
        const hasRole = interaction.member.roles.cache.some(role => config.roles.admin.includes(role.id));
        if (!hasRole) {
            return interaction.reply({ content: '❌ You do not have permission to use this command.', flags: MessageFlags.Ephemeral });
        }

        const channel = interaction.options.getChannel('channel');
        const typeKey = interaction.options.getString('type');
        const messageRaw = interaction.options.getString('message');
        const pingType = interaction.options.getString('ping') || 'none';
        const image = interaction.options.getAttachment('image');
        const customFooter = interaction.options.getString('footer');

        const messageContent = messageRaw.replace(/\\n/g, '\n');
        const typeData = announceTypes[typeKey];

        const embed = new EmbedBuilder()
            .setColor(typeData.color)
            .setTitle(`${typeData.emoji}  ${typeData.title}`)
            .setDescription(messageContent)
            .setFooter({ text: customFooter || 'Hero Client • Official Broadcast' })
            .setTimestamp();

        if (image) {
            embed.setImage(image.url);
        }

        let content = '';
        if (pingType === 'everyone') content = '@everyone';
        if (pingType === 'here') content = '@here';
        if (pingType === 'client') content = `<@&${config.roles.client}>`;

        try {
            await channel.send({ content: content || null, embeds: [embed] });
            await interaction.reply({ content: `✅ **${typeData.title}** sent to ${channel}!`, flags: MessageFlags.Ephemeral });
        } catch (error) {
            console.error(error);
            await interaction.reply({ content: '❌ Failed to send announcement. Check bot permissions.', flags: MessageFlags.Ephemeral });
        }
    },
};
