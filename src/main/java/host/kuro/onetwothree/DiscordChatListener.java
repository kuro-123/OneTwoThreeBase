package host.kuro.onetwothree;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.command.defaults.ListCommand;
import host.kuro.onetwothree.task.RebootTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordChatListener extends ListenerAdapter {

    private final OneTwoThreeAPI api;
    private JDA jda;
    private String channelId;

    public DiscordChatListener(OneTwoThreeAPI api) {
        this.api = api;
        jda = this.api.getPlugin().getJDA();
        channelId = this.api.getPlugin().getChannelID();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if (api.getDebug()) return;
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
        // やまびこは避けるw
        if (message.indexOf("[鯖内]") >= 0) return;

        if (message.startsWith("/")) {
            // コマンド
            CommnadExecuteFromDiscord(message, author);
        } else {
            // チャット
            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.LIGHT_PURPLE);
            sb.append("[");
            sb.append("ﾃﾞｨｽｺ");
            sb.append("] <");
            sb.append(TextFormat.WHITE);
            sb.append(author);
            sb.append(TextFormat.LIGHT_PURPLE);
            sb.append("> ");
            sb.append(TextFormat.WHITE);
            sb.append(message);
            api.getServer().broadcastMessage(new String(sb));
        }
    }

    private void CommnadExecuteFromDiscord(String message, String author) {
        if (message.startsWith("/list")) {
            String buff = ListCommand.GetListString(null);
            api.getMessage().SendDiscordBlueMessage("\n" + buff);
        }
        else if (message.startsWith("/ver")) {
            String buff = api.getMessage().GetVersionInfo();
            api.getMessage().SendDiscordBlueMessage("\n" + buff);
        }
        else if (message.startsWith("/reboot")) {
            if (author.equals("くろ")) { //　とりあえず、後々権限取得
                // ブロードキャスト通知
                StringBuilder sb = new StringBuilder();
                sb.append(TextFormat.GOLD);
                sb.append(TextFormat.BOLD);
                sb.append("==== [再起動] ====");
                sb.append(TextFormat.GOLD);
                api.getMessage().SendErrorMessage(new String(sb), true);
                // リブートタスク起動
                api.getServer().getScheduler().scheduleRepeatingTask(new RebootTask(api), 200);
            } else {
                api.getMessage().SendErrorMessage("鯖主以外が/reboot命令はできません", true);
            }
        }
    }
}