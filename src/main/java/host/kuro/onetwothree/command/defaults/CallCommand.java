package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.task.SoundTask;

import java.util.HashMap;

public class CallCommand extends CommandBase {

    public CallCommand(OneTwoThreeAPI api) {
        super("call", api);
        this.setAliases(new String[]{"ca"});
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;
        if (player == null) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        try {
            // 権限チェック
            if (!api.IsJyumin(player)) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                return false;
            }
            // スキルチェック
            if (!api.IsGameMaster(player)) {
                int count = api.GetChatCount(player);
                if (count < 7000) {
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    player.sendMessage(api.GetWarningMessage("onetwothree.skill_err"));
                    return false;
                }
            }
            // クールダウンチェック
            if (OneTwoThreeAPI.systemcall_timing != 0) {
                long now = System.currentTimeMillis();
                long keika = now - OneTwoThreeAPI.systemcall_timing;
                if (keika < (1000 * 60 * 15)) { // 15分クール
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    this.sendUsage(sender);
                    return false;
                }
            }
            OneTwoThreeAPI.systemcall_timing = System.currentTimeMillis();

            // コールメッセージをツイートする
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
            String message = new String(sb_b);
            api.getServer().broadcastMessage(message);
            api.sendDiscordYellowMessage(message);

            player.sendMessage(api.GetInfoMessage("commands.call.success"));

        } catch (Exception e) {
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, api.GetErrorMessage(e));
            return false;
        }
        return true;
    }
}