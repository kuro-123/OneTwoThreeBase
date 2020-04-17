package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.task.SoundTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Date;

public class Message {

    protected OneTwoThreeAPI api;

    public Message(OneTwoThreeAPI api) {
        this.api = api;
    }

    // プラグインタイトルメッセージ
    public String GetMessageTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.WHITE);
        sb.append("[");
        sb.append(Language.translate("onetwothree.name"));
        sb.append("] ");
        return new String(sb);
    }

    // 情報メッセージ
    public void SendInfoMessage(String target, Player player) {
        SendInfoMessage(target, player, SoundTask.jin054);
    }
    public void SendInfoMessage(String target, Player player, String sound) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.WHITE);
        sb.append(target);
        String message = new String(sb);
        api.PlaySound(player, SoundTask.MODE_PLAYER, sound, 0, false); // SUCCESS
        player.sendMessage(message);
    }
    public void SendBroadcastInfoMessage(String target) {
        SendBroadcastInfoMessage(target, SoundTask.jin055);
    }
    public void SendBroadcastInfoMessage(String target, String sound) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.WHITE);
        sb.append(target);
        String message = new String(sb);
        api.PlaySound(null, SoundTask.MODE_BROADCAST, sound, 0, false); // SUCCESS
        api.getServer().broadcastMessage(message);
        SendDiscordGreenMessage(message);
    }

    // 警告メッセージ
    public void SendWarningMessage(String target) {
        SendWarningMessage(target, null, false);
    }
    public void SendWarningMessage(String target, boolean withDiscord) {
        SendWarningMessage(target, null, withDiscord);
    }
    public void SendWarningMessage(String target, Player player) {
        SendWarningMessage(target, player, false);
    }
    private void SendWarningMessage(String target, Player player, boolean withDiscord) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.YELLOW);
        sb.append(target);
        String message = new String(sb);
        if (player == null) {
            // BROADCAST
            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin007, 0, false); // FAIL
            api.getServer().broadcastMessage(message);
            if (withDiscord) {
                SendDiscordYellowMessage(message);
            }
        } else {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(message);
        }
    }

    // エラーメッセージ
    public void SendErrorMessage(String target) {
        SendErrorMessage(target, null, false);
    }
    public void SendErrorMessage(String target, boolean withDiscord) {
        SendErrorMessage(target, null, withDiscord);
    }
    public void SendErrorMessage(String target, Player player) {
        SendErrorMessage(target, player, false);
    }
    private void SendErrorMessage(String target, Player player, boolean withDiscord) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.RED);
        sb.append(target);
        String message = new String(sb);
        if (player == null) {
            // BROADCAST
            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // BUBU-
            api.getServer().broadcastMessage(message);
            if (withDiscord) {
                SendDiscordRedMessage(message);
            }
        } else {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin001, 0, false); // BUBU-
            player.sendMessage(message);
        }
    }

    public void SendDiscordMessage(Player player, String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```diff\n");
                sb.append("  ");
                sb.append(chat_time);
                sb.append(" [鯖内] <");
                sb.append(player.getDisplayName());
                sb.append("> ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(player, GetErrorMessage(e));
        }
    }
    private void SendDiscordRedMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```diff\n");
                sb.append("- ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    public void SendDiscordBlueMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                sb.append("# ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    private void SendDiscordGreenMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```xl\n");
                sb.append("' ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    private void SendDiscordYellowMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```diff\n");
                sb.append("+ ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    private void SendDiscordGrayMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```py\n");
                sb.append("# ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    private void SendDiscordBanMessage(String message) {
        if (api.getDebug()) return;
        try {
            JDA jda = api.getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(api.getPlugin().getBanChannelID());
            if (channel != null) {
                String chat_time = api.sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```diff\n");
                sb.append("- ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, GetErrorMessage(e));
        }
    }
    private String CutSection(String message) {
        String ret = message;
        ret = ret.replace(""+TextFormat.BLACK, "");
        ret = ret.replace(""+TextFormat.DARK_BLUE, "");
        ret = ret.replace(""+TextFormat.DARK_GREEN, "");
        ret = ret.replace(""+TextFormat.DARK_AQUA, "");
        ret = ret.replace(""+TextFormat.DARK_RED, "");
        ret = ret.replace(""+TextFormat.DARK_PURPLE, "");
        ret = ret.replace(""+TextFormat.GOLD, "");
        ret = ret.replace(""+TextFormat.GRAY, "");
        ret = ret.replace(""+TextFormat.DARK_GRAY, "");
        ret = ret.replace(""+TextFormat.BLUE, "");
        ret = ret.replace(""+TextFormat.GREEN, "");
        ret = ret.replace(""+TextFormat.AQUA, "");
        ret = ret.replace(""+TextFormat.RED, "");
        ret = ret.replace(""+TextFormat.LIGHT_PURPLE, "");
        ret = ret.replace(""+TextFormat.YELLOW, "");
        ret = ret.replace(""+TextFormat.WHITE, "");
        ret = ret.replace(""+TextFormat.MINECOIN_GOLD, "");
        ret = ret.replace(""+TextFormat.OBFUSCATED, "");
        ret = ret.replace(""+TextFormat.BOLD, "");
        ret = ret.replace(""+TextFormat.STRIKETHROUGH, "");
        ret = ret.replace(""+TextFormat.UNDERLINE, "");
        ret = ret.replace(""+TextFormat.ITALIC, "");
        ret = ret.replace(""+TextFormat.RESET, "");
        return ret;
    }

    public void SendBlockInfoMessage(Player player, Block block) {
        if (block == null) return;
        String id = ((Integer)block.getId()).toString();
        String meta = ((Integer)block.getDamage()).toString();
        String name = block.getName();
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.WHITE);
        sb.append(name);
        sb.append(" -> ");
        sb.append(TextFormat.GREEN);
        sb.append(id);
        sb.append(":");
        sb.append(meta);
        sb.append(TextFormat.YELLOW);
        sb.append(") - ");
        sb.append("位置(");
        sb.append(" X:" + block.getFloorX());
        sb.append(" Y:" + block.getFloorY());
        sb.append(" Z:" + block.getFloorZ());
        sb.append(")");
        SendInfoMessage(new String(sb), player);
    }

    public void SendBanMessage(Player player, String banName) {
        StringBuilder sb = new StringBuilder();
        if (!api.IsNushi(player)) {
            sb.append(TextFormat.RED);
            sb.append("[BANアイテム警告] ");
            sb.append(TextFormat.WHITE);
            sb.append(" [ ");
            sb.append(TextFormat.YELLOW);
            sb.append(player.getDisplayName());
            sb.append(" 位置:");
            sb.append(player.getLevel().getName());
            sb.append(" x:");
            sb.append(player.getFloorX());
            sb.append(" y:");
            sb.append(player.getFloorY());
            sb.append(" z:");
            sb.append(player.getFloorZ());
            sb.append(TextFormat.WHITE);
            sb.append(" ] さんが [ ");
            sb.append(TextFormat.YELLOW);
            sb.append(banName);
            sb.append(TextFormat.WHITE);
            sb.append(" ] を使おうとしています！");
            sb.append(TextFormat.RED);
            sb.append(" ご注意ください！");
            SendErrorMessage(new String(sb), true);
        }
    }

    public void SendZoneMessage(Player player, String rank) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.YELLOW);
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.YELLOW);
        sb.append(" ] により、 [ ");
        sb.append(TextFormat.RED);
        sb.append(rank);
        sb.append(TextFormat.YELLOW);
        sb.append(" ] ランクゾーンが設定されました！");
        SendBroadcastInfoMessage(new String(sb));
    }

    public void SendDropMessage(Player player, String item) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.YELLOW);
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.YELLOW);
        sb.append(" ] さんの近くに [ ");
        sb.append(TextFormat.RED);
        sb.append(item);
        sb.append(TextFormat.YELLOW);
        sb.append(" ] がドロップした！");
        SendBroadcastInfoMessage(new String(sb));
    }

    public String GetErrorMessage(Exception ex) {
        StackTraceElement[] ste = ex.getStackTrace();
        String buff = "";
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element: ste) {
            sb.append("[");
            sb.append(element);
            sb.append("]\n");
        }
        buff = (ex.getClass().getName() + ": "+ ex.getMessage() + " -> " + new String(sb));
        if (buff.length() > 2000) {
            buff = buff.substring(0, 2000);
        }
        return buff;
    }

    public String SendJoinMessage(Player player) {
        StringBuilder sb_join = new StringBuilder();
        sb_join.append(TextFormat.YELLOW);
        sb_join.append(player.getDisplayName());
        sb_join.append("さん ");
        sb_join.append(api.GetRankColor(player));
        sb_join.append("<");
        sb_join.append(api.GetRankName(player));
        sb_join.append("> ");
        sb_join.append(TextFormat.YELLOW);
        sb_join.append("が参加しました");
        String message = new String(sb_join);
        api.getMessage().SendDiscordBlueMessage(message);
        return message;
    }

    public String SendQuitMessage(Player player) {
        StringBuilder sb_join = new StringBuilder();
        sb_join.append(TextFormat.YELLOW);
        sb_join.append(player.getDisplayName());
        sb_join.append("さん ");
        sb_join.append(api.GetRankColor(player));
        sb_join.append("<");
        sb_join.append(api.GetRankName(player));
        sb_join.append("> ");
        sb_join.append(TextFormat.YELLOW);
        sb_join.append("が退出しました");
        String message = new String(sb_join);
        api.getMessage().SendDiscordGrayMessage(message);
        return message;
    }

    public String SendDeathMessage(Player player, EntityDamageEvent cause, String killer, String killitem) {
        String cause_name = "";
        String message = "";
        if (cause == null) return "";
        EntityDamageEvent.DamageCause causeid = cause.getCause();
        switch (causeid) {
            case ENTITY_ATTACK: cause_name = "死因: 攻撃"; break;
            case PROJECTILE: cause_name = "死因: 射抜"; break;
            case SUICIDE: cause_name = "死因: 自殺"; break;
            case VOID: cause_name = "死因: 奈落"; break;
            case FALL: cause_name = "死因: 落下"; break;
            case SUFFOCATION: cause_name = "死因: 窒息"; break;
            case LAVA: cause_name = "死因: 溶岩"; break;
            case FIRE: cause_name = "死因: 焼死"; break;
            case FIRE_TICK: cause_name = "死因: 炎"; break;
            case DROWNING: cause_name = "死因: 溺死"; break;
            case CONTACT: cause_name = "死因: サボテン"; break;
            case BLOCK_EXPLOSION: cause_name = "死因: 爆発"; break;
            case ENTITY_EXPLOSION: cause_name = "死因: 爆発"; break;
            case MAGIC: cause_name = "死因: 魔法"; break;
            case LIGHTNING: cause_name = "死因: 雷"; break;
            default: cause_name = "死因: 不明"; break;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.YELLOW);
        sb.append("[ ");
        sb.append(player.getDisplayName());
        sb.append(" さん ] が あひ～！ [ ");
        sb.append(TextFormat.RED);
        sb.append(cause_name);
        sb.append(TextFormat.YELLOW);
        sb.append(" ]");
        if (killer.length() > 0) {
            sb.append(TextFormat.YELLOW);
            sb.append(" [ 殺害者: ");
            sb.append(TextFormat.RED);
            sb.append(killer);
            sb.append(TextFormat.YELLOW);
            sb.append(" ]");
        }
        if (killitem.length() > 0) {
            sb.append(TextFormat.YELLOW);
            sb.append(" [ ｱｲﾃﾑ: ");
            sb.append(TextFormat.RED);
            sb.append(killitem);
            sb.append(TextFormat.YELLOW);
            sb.append(" ]");
        }
        Particle.SpiralFlame(player, player.getLevel(), player.getX(), player.getY(), player.getZ());
        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin002, 0, false); // ボンッ
        message = new String(sb);
        SendDiscordYellowMessage(message);
        return message;
    }

    public void SendForceLogout(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.RED);
        sb.append("【強制ログアウト】 [");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.RED);
        sb.append("] さんがゲームモードを変更しようとしたため強制的にログアウトされました");
        String message = new String(sb);
        SendErrorMessage(message, true);
    }

    public void SendAutoKick(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.YELLOW);
        sb.append("【自動キック】 ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.YELLOW);
        sb.append(" さんは、長時間放置していたため自動キックされました");
        SendWarningMessage(new String(sb), true);
    }

    public String GetSellMessage(Item item, int price) {
        String item_name = item.getName();
        int suryo = item.getCount();
        int kingaku = price * suryo;
        StringBuilder sb = new StringBuilder();
        sb.append("手持ちアイテム [ ");
        sb.append(item_name);
        sb.append(" ]\n\n");
        sb.append("数量 : " + suryo + "個\n");
        sb.append("金額 : " + kingaku + "p\n\n");
        sb.append("売却しますか？");
        return new String(sb);
    }

    public void SendSelledMessage(Player player, String item, int suryo, int kingaku) {
        StringBuilder sb = new StringBuilder();
        sb.append("手持ちアイテム [ ");
        sb.append(TextFormat.YELLOW);
        sb.append(item);
        sb.append(" ");
        sb.append(api.comma_format.format(suryo) + "個 ");
        sb.append(TextFormat.WHITE);
        sb.append("] を [ ");
        sb.append(TextFormat.YELLOW);
        sb.append(api.comma_format.format(kingaku));
        sb.append("p ");
        sb.append(TextFormat.WHITE);
        sb.append("] で売却しました！");
        player.sendMessage(new String(sb));
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin071, 0, false); // レジスタ
    }

    public void SendTeleportMessage(Player player, String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append("[ﾜｰﾌﾟ] ");
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ] -> [ ");
        sb.append(TextFormat.WHITE);
        sb.append(target);
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ]");
        String message = new String(sb);
        api.getMessage().SendBroadcastInfoMessage(message, SoundTask.jin020);
    }

    public void SendUsage(CommandBase base, CommandSender sender) {
        base.sendUsage(sender);
        if(sender instanceof Player) {
            api.PlaySound((Player)sender, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
        }
    }

    public void SendSystemCall(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("【システムコール】\n");
        sb.append(player.getDisplayName());
        sb.append("さんからコールです!!\n(ゲーム内の場所 : ");
        sb.append(player.getLevel().getName());
        sb.append(" x:" + player.getPosition().getFloorX());
        sb.append(" y:" + player.getPosition().getFloorY());
        sb.append(" z:" + player.getPosition().getFloorZ());
        sb.append(")\n #123鯖");
        api.getTwitter().Tweet(new String(sb));

        StringBuilder sb_b = new StringBuilder();
        sb_b.append(TextFormat.YELLOW);
        sb_b.append(player.getDisplayName());
        sb_b.append("さんがシステムコールしました！ (公式ツイート ※クールタイム15分)");
        api.getMessage().SendBroadcastInfoMessage(new String(sb_b));
    }

    public void SendPayMessage(Player player, Player responser, int money, int pay) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.YELLOW);
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(responser.getDisplayName());
        sb.append(TextFormat.YELLOW);
        sb.append(" ] さんに [ ");
        sb.append(TextFormat.WHITE);
        sb.append(api.comma_format.format(pay));
        sb.append("p ");
        sb.append(TextFormat.YELLOW);
        sb.append("] 支払いました。現在の所持金は [ ");
        sb.append(TextFormat.WHITE);
        sb.append(api.comma_format.format(money-pay));
        sb.append("p ");
        sb.append(TextFormat.YELLOW);
        sb.append("] です");
        api.getMessage().SendInfoMessage(new String(sb), player, SoundTask.jin071);
    }
    public void SendPayFromMessage(Player player, Player responser, int pay) {
        int nowmoney = api.GetMoney(responser);
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.AQUA);
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.AQUA);
        sb.append(" ] さんから [ ");
        sb.append(TextFormat.WHITE);
        sb.append(api.comma_format.format(pay));
        sb.append("p ");
        sb.append(TextFormat.AQUA);
        sb.append("] が支払われました。現在の所持金は [ ");
        sb.append(TextFormat.WHITE);
        sb.append(api.comma_format.format(nowmoney));
        sb.append("p ");
        sb.append(TextFormat.AQUA);
        sb.append("] です");
        api.getMessage().SendInfoMessage(new String(sb), responser, SoundTask.jin071);
    }
}
