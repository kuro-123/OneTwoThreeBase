package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.LagTask;
import host.kuro.onetwothree.task.RebootTask;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LagCommand extends CommandBase {

    private Config cfg;

    public LagCommand(OneTwoThreeAPI api) {
        super("lag", api);
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        // 権限チェック
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) {
            player = (Player) sender;
            if (!api.IsKanri(player)) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                return false;
            }
        }
        if (api.lag_task_id == -1) {
            String mes = TextFormat.RED + Language.translate("commands.lag.err_execute");
            player.sendMessage(mes);
            this.api.getServer().getLogger().warning(mes);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        // ラグタスク起動
        LagTask task = new LagTask(api);
        TaskHandler th = api.getServer().getScheduler().scheduleRepeatingTask(task, 200);
        api.lag_task_id = th.getTaskId();
        return true;
    }
}