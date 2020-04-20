package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.task.RebootTask;
import host.kuro.onetwothree.task.SoundTask;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class RebootCommand extends CommandBase {

    public RebootCommand(OneTwoThreeAPI api) {
        super("reboot", api);
        this.setAliases(new String[]{"re"});
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;

        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;

        if (player == null) {
            // ブロードキャスト通知
            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.GOLD);
            sb.append(TextFormat.BOLD);
            sb.append("==== [鯖主再起動] ====");
            sb.append(TextFormat.GOLD);
            String message = new String(sb);
            api.getMessage().SendErrorMessage(message, true);

            // リブートタスク起動
            api.getServer().getScheduler().scheduleRepeatingTask(new RebootTask(api), 200);
        }
        return true;
    }
}