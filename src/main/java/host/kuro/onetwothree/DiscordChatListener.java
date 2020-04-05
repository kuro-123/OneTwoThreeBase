package host.kuro.onetwothree;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordChatListener extends ListenerAdapter {

    private final OneTwoThreeAPI api;
    private JDA jda;
    private String channelId;
    private Config config;

    public DiscordChatListener(OneTwoThreeAPI api) {
        this.api = api;
        jda = this.api.getPlugin().getJDA();
        channelId = this.api.getPlugin().getChannelID();
        config = this.api.getConfig();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        // JDA
        if (jda == null) return;
        if (jda.getSelfUser() == null) return;
        if (jda.getSelfUser().getId() == null) return;
        String selfname = jda.getSelfUser().getName();
        // Author
        if (e.getAuthor() == null) return;
        if (e.getAuthor().getId() == null) return;
        String author = e.getAuthor().getName();
        // OWN
        if (e.getAuthor().equals(jda.getSelfUser())) return;
        // メンバー
        if (e.getMember() == null) return;
        // チャンネルチェック
        if (!e.getChannel().getId().equals(channelId)) return;
        // メッセージ取得
        String message = TextFormat.clean(e.getMessage().getContentStripped());
        if (message.isEmpty() && e.getMessage().getAttachments().isEmpty()) return;
        // 長文は削る
        if (message.length() > 128) message = message.substring(0, 124);

        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.WHITE);
        sb.append("[");
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append("DISCORD");
        sb.append(TextFormat.WHITE);
        sb.append("] <");
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(author);
        sb.append(TextFormat.WHITE);
        sb.append("> ");
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(message);
        api.getServer().broadcastMessage(new String(sb));
    }
}