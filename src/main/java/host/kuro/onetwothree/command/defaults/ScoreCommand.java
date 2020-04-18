package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.NpcPlugin;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.datatype.NpcInfo;
import host.kuro.onetwothree.datatype.ScoreInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.npc.NpcType;
import host.kuro.onetwothree.scoreboard.Score;
import host.kuro.onetwothree.task.ScoreTask;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class ScoreCommand extends CommandBase {

    public ScoreCommand(OneTwoThreeAPI api) {
        super("score", api);
        this.setAliases(new String[]{"sc"});
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;
        if (player == null) {
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        // 権限チェック
        if (!api.IsJyumin(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        return ScoreWindow(player);
    }

    private boolean ScoreWindow(Player player) {
        try {
            ArrayList<String> mode_list = new ArrayList<String>();
            mode_list.add("非表示にする");
            mode_list.add("自分のステータス");
            mode_list.add("所持金ランキング");
            mode_list.add("整地ランキング");
            mode_list.add("建築ランキング");
            mode_list.add("ログインランキング");
            mode_list.add("キックされランキング");
            mode_list.add("死にすぎランキング");
            mode_list.add("殺しすぎランキング");
            mode_list.add("噺家ランキング");
            mode_list.add("プレイ時間ランキング");
            CustomForm form = new CustomForm(Language.translate("commands.score.title"))
                    .addLabel(Language.translate("commands.score.message"))
                    .addDropDown(Language.translate("commands.score.list"), mode_list);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, Language.translate("commands.score.title"), data.get(1).toString(), "","","","", "", targetPlayer.getDisplayName());

                    // タイプ
                    String buff = data.get(1).toString();
                    ScoreInfo si = new ScoreInfo();
                    si.exec_time = 0;
                    switch (buff) {
                        case "非表示にする": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_NONE; break;
                        case "自分のステータス": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_SELF; break;
                        case "所持金ランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_MONEY; break;
                        case "整地ランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_BREAK; break;
                        case "建築ランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_PLACE; break;
                        case "ログインランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_LOGIN; break;
                        case "キックされランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_KICK; break;
                        case "死にすぎランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_DEATH; break;
                        case "殺しすぎランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_KILL; break;
                        case "噺家ランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_CHAT; break;
                        case "プレイ時間ランキング": si.mode = ScoreTask.SCORE_MODE.MODE_SCORE_PLAYTIME; break;
                        default:
                            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), targetPlayer);
                            return;
                    }
                    api.score_info.put(targetPlayer, si);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS

                } catch (Exception e) {
                    api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), targetPlayer);
                    api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }
}