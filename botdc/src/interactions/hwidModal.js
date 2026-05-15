const { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle, ChannelType, PermissionFlagsBits, MessageFlags } = require('discord.js');
const config = require('../../config.json');

module.exports = {
    customId: 'hwid_modal',
    async execute(interaction, client) {
        const key = interaction.fields.getTextInputValue('hwid_key');
        const username = interaction.fields.getTextInputValue('hwid_user');
        const guild = interaction.guild;
        const user = interaction.user;

        await interaction.deferReply({ flags: MessageFlags.Ephemeral });

        // Find category for HWID
        let category;
        const configCategory = config.categories.resethwid;

        if (configCategory.match(/^\d+$/)) {
            category = guild.channels.cache.get(configCategory);
        } else {
            category = guild.channels.cache.find(c => c.name.toLowerCase() === configCategory.toLowerCase() && c.type === ChannelType.GuildCategory);
        }

        if (!category) {
            return interaction.editReply({ content: '⚠️ Error: Category for **Reset HWID** not found.' });
        }

        try {
            const channelName = `🎫-hwid-${user.username}`.toLowerCase();
            const channel = await guild.channels.create({
                name: channelName,
                type: ChannelType.GuildText,
                parent: category.id,
                permissionOverwrites: [
                    { id: guild.id, deny: [PermissionFlagsBits.ViewChannel] },
                    { id: user.id, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.AttachFiles] },
                    { id: config.roles.support, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages] },
                ],
            });

            const ticketEmbed = new EmbedBuilder()
                .setColor(config.colors.gold)
                .setAuthor({ name: 'HWID RESET SESSION', iconURL: guild.iconURL() })
                .setTitle('Hardware Reset Request')
                .setDescription(`Hello ${user}, your reset request has been initialized. Staff will review the provided data shortly.`)
                .addFields(
                    { name: '🔑 | License Key', value: `\`\`\`${key}\`\`\``, inline: false },
                    { name: '👤 | Client User', value: `\`\`\`${username}\`\`\``, inline: false },
                    { name: '📋 | Session Data', value: `\`\`\`yaml\nUser: ${user.tag}\nID: ${user.id}\nCategory: HWID RESET\nStatus: Awaiting Staff\n\`\`\`` }
                )
                .setFooter({ text: 'Hero Client • Hardware Management' });

            const closeButton = new ButtonBuilder()
                .setCustomId('close_ticket')
                .setLabel('Close')
                .setStyle(ButtonStyle.Danger)
                .setEmoji('🔒');

            const pingButton = new ButtonBuilder()
                .setCustomId('ping_user')
                .setLabel('Ping User')
                .setStyle(ButtonStyle.Secondary)
                .setEmoji('🔔');

            const row = new ActionRowBuilder().addComponents(closeButton, pingButton);

            await channel.send({ content: `<@${interaction.user.id}>`, embeds: [ticketEmbed], components: [row] });
            await interaction.editReply({ content: `✅ Ticket created: ${channel}` });

        } catch (error) {
            console.error(error);
            await interaction.editReply({ content: '❌ Failed to create ticket. Check bot permissions.' });
        }
    },
};
