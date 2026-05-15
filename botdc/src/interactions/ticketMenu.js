const { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle, ChannelType, PermissionFlagsBits, MessageFlags, ModalBuilder, TextInputBuilder, TextInputStyle } = require('discord.js');
const config = require('../../config.json');

const cooldowns = new Map();

module.exports = {
    customId: 'ticket_select',
    async execute(interaction, client) {
        const selectedValue = interaction.values[0];
        const guild = interaction.guild;
        const user = interaction.user;

        // 1. Cooldown Check (60 seconds)
        const now = Date.now();
        const cooldownAmount = 60 * 1000;
        if (cooldowns.has(user.id)) {
            const expirationTime = cooldowns.get(user.id) + cooldownAmount;
            if (now < expirationTime) {
                const timeLeft = (expirationTime - now) / 1000;
                return interaction.reply({ content: `⏳ Please wait **${timeLeft.toFixed(1)}s** before opening another ticket.`, flags: MessageFlags.Ephemeral });
            }
        }

        // 2. Ticket Limit Check (Max 2)
        const userTickets = guild.channels.cache.filter(c => c.name.includes(user.username.toLowerCase()) && c.name.startsWith('🎫-')).size;
        if (userTickets >= 2) {
            return interaction.reply({ content: '❌ You already have **2 open tickets**. Please close one before opening a new one.', flags: MessageFlags.Ephemeral });
        }

        // Check for modal requirement
        if (selectedValue === 'resethwid') {
            const modal = new ModalBuilder()
                .setCustomId('hwid_modal')
                .setTitle('Hardware Reset Request');

            const keyInput = new TextInputBuilder()
                .setCustomId('hwid_key')
                .setLabel('License Key')
                .setPlaceholder('Enter your license key here...')
                .setStyle(TextInputStyle.Short)
                .setRequired(true);

            const userInput = new TextInputBuilder()
                .setCustomId('hwid_user')
                .setLabel('Client Username')
                .setPlaceholder('Enter your client username...')
                .setStyle(TextInputStyle.Short)
                .setRequired(true);

            const firstActionRow = new ActionRowBuilder().addComponents(keyInput);
            const secondActionRow = new ActionRowBuilder().addComponents(userInput);

            modal.addComponents(firstActionRow, secondActionRow);
            return await interaction.showModal(modal);
        }

        // Find category by ID first, then fallback to name
        let category;
        const configCategory = config.categories[selectedValue];

        if (configCategory.match(/^\d+$/)) {
            category = guild.channels.cache.get(configCategory);
        } else {
            category = guild.channels.cache.find(c => c.name.toLowerCase() === configCategory.toLowerCase() && c.type === ChannelType.GuildCategory);
        }

        if (!category) {
            return interaction.reply({ content: `⚠️ Error: Category for **${selectedValue}** not found. Please check config/IDs.`, flags: MessageFlags.Ephemeral });
        }

        await interaction.deferReply({ flags: MessageFlags.Ephemeral });

        try {
            const channelName = `🎫-${selectedValue}-${user.username}`.toLowerCase();
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

            // Set Cooldown
            cooldowns.set(user.id, now);

            const ticketEmbed = new EmbedBuilder()
                .setColor(config.colors.gold)
                .setAuthor({ name: 'TICKET ACTIVE', iconURL: guild.iconURL() })
                .setTitle(`Department: ${selectedValue.toUpperCase()}`)
                .setDescription(`Hello ${user}, thank you for reaching out to **Hero Client**. A staff member will be with you shortly.`)
                .addFields(
                    { name: '📋 | Session Data', value: `\`\`\`yaml\nUser: ${user.tag}\nID: ${user.id}\nCategory: ${selectedValue.toUpperCase()}\nStatus: Awaiting Staff\n\`\`\`` },
                    { name: '⚖️ | Ticket Rules', value: '1. Please be descriptive of your issue.\n2. Do not ping staff roles repeatedly.\n3. Send any relevant screenshots now.' }
                )
                .setFooter({ text: 'Hero Client • Your security is our priority' });

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
