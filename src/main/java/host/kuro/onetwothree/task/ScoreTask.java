package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.ScoreInfo;
import host.kuro.onetwothree.scoreboard.Criteria;
import host.kuro.onetwothree.scoreboard.DisplaySlot;
import host.kuro.onetwothree.scoreboard.Scoreboard;
import host.kuro.onetwothree.scoreboard.ScoreboardObjective;
import org.w3c.dom.Text;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class ScoreTask extends AsyncTask {

    private final OneTwoThreeAPI api;
    private final Scoreboard scoreboard;
    private final ScoreboardObjective objective;
    private int SEQ = 0;
    public enum SCORE_MODE {
        MODE_SCORE_NONE,
        MODE_SCORE_SELF,
        MODE_SCORE_MONEY,
        MODE_SCORE_BREAK,
        MODE_SCORE_PLACE,
        MODE_SCORE_LOGIN,
        MODE_SCORE_KICK,
        MODE_SCORE_DEATH,
        MODE_SCORE_KILL,
        MODE_SCORE_CHAT,
        MODE_SCORE_PLAYTIME,
    };
    private static SCORE_MODE mode = SCORE_MODE.MODE_SCORE_SELF;

    public ScoreTask(OneTwoThreeAPI api) {
        this.api = api;
        scoreboard = new Scoreboard();
        objective = scoreboard.registerNewObjective("side_obj", Criteria.DUMMY);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public void onRun() {
        SEQ++;
        for (Player player : api.getServer().getOnlinePlayers().values()) {
            if (api.score_info == null) continue;
            if (!api.score_info.containsKey(player)) continue;

            ScoreInfo si = api.score_info.get(player);
            if (si.mode == SCORE_MODE.MODE_SCORE_NONE) {
                if (objective.scores.size() > 0) {
                    objective.scores.clear();
                    objective.setDisplayName("");
                    scoreboard.player = player;
                    scoreboard.onRemove();
                }
                continue;
            }

            switch (si.mode) {
                case MODE_SCORE_SELF:
                    if (si.exec_time != 0) {
                        long passed = System.currentTimeMillis() - si.exec_time;
                        if (passed < 10000) {
                            continue;
                        }
                    }
                    UpdatePlayerScore(player);
                    break;
                case MODE_SCORE_MONEY:
                case MODE_SCORE_BREAK:
                case MODE_SCORE_PLACE:
                case MODE_SCORE_LOGIN:
                case MODE_SCORE_KICK:
                case MODE_SCORE_DEATH:
                case MODE_SCORE_KILL:
                case MODE_SCORE_CHAT:
                case MODE_SCORE_PLAYTIME:
                    if (si.exec_time != 0) {
                        long passed = System.currentTimeMillis() - si.exec_time;
                        if (passed < 30000) {
                            continue;
                        }
                    }
                    UpdateRanking(player, si.mode);
                    break;
            }
            si.exec_time = System.currentTimeMillis();
            api.score_info.put(player, si);
        }
    }

    private void ResetRegister() {
        for (int i=1; i<=10; i++) {
            objective.resetScore(""+i);
        }
    }

    public void UpdatePlayerScore(Player player) {
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0049"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    objective.scores.clear();
                    objective.setDisplayName(TextFormat.GREEN + "ステータス");
                    switch (rs.getInt("rank")) {
                        case 0: objective.setDisplayName(TextFormat.GREEN + "権限: 訪問"); break;
                        case 1: objective.setDisplayName(TextFormat.GREEN + "権限: 住民"); break;
                        case 2: objective.setDisplayName(TextFormat.GREEN + "権限: ＧＭ"); break;
                        case 3: objective.setDisplayName(TextFormat.GREEN + "権限: パイ"); break;
                        case 4: objective.setDisplayName(TextFormat.GREEN + "権限: 鯖主"); break;
                        default: objective.setDisplayName(TextFormat.GREEN + "権限: その他"); break;
                    }
                    objective.registerScore("1", TextFormat.YELLOW + "所持金: ", 0);
                    objective.registerScore("2", "ログイン: ", 0);
                    objective.registerScore("3", "整地: ", 0);
                    objective.registerScore("4", "建築: ", 0);
                    objective.registerScore("5", "殺害: ", 0);
                    objective.registerScore("6", "キック: ", 0);
                    objective.registerScore("7", "死亡: ", 0);
                    objective.registerScore("8", "チャット: ", 0);
                    objective.setScore("1", rs.getInt("money"));
                    objective.setScore("2", rs.getInt("login"));
                    objective.setScore("3", rs.getInt("break"));
                    objective.setScore("4", rs.getInt("place"));
                    objective.setScore("5", rs.getInt("kill"));
                    objective.setScore("6", rs.getInt("kick"));
                    objective.setScore("7", rs.getInt("death"));
                    objective.setScore("8", rs.getInt("chat"));
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            scoreboard.player = player;
            scoreboard.onUpdate(ScoreboardObjective.Descending);

        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return;
        }
    }

    private void UpdateRanking(Player player, SCORE_MODE mode) {
        try {
            objective.scores.clear();

            String exec_sql = "";
            String field = "";
            switch (mode) {
                case MODE_SCORE_MONEY: exec_sql = "Sql0068"; objective.setDisplayName(TextFormat.YELLOW + "所持金ランキング"); break;
                case MODE_SCORE_BREAK: exec_sql = "Sql0069"; objective.setDisplayName(TextFormat.YELLOW + "整地ランキング"); break;
                case MODE_SCORE_PLACE: exec_sql = "Sql0070"; objective.setDisplayName(TextFormat.YELLOW + "建築ランキング"); break;
                case MODE_SCORE_LOGIN: exec_sql = "Sql0071"; objective.setDisplayName(TextFormat.YELLOW + "ログインランキング"); break;
                case MODE_SCORE_KICK: exec_sql = "Sql0072"; objective.setDisplayName(TextFormat.YELLOW + "キックされランキング"); break;
                case MODE_SCORE_DEATH: exec_sql = "Sql0073"; objective.setDisplayName(TextFormat.YELLOW + "死にすぎランキング"); break;
                case MODE_SCORE_KILL: exec_sql = "Sql0074"; objective.setDisplayName(TextFormat.YELLOW + "殺しすぎランキング"); break;
                case MODE_SCORE_CHAT: exec_sql = "Sql0075"; objective.setDisplayName(TextFormat.YELLOW + "噺家ランキング"); break;
                case MODE_SCORE_PLAYTIME: exec_sql = "Sql0076"; objective.setDisplayName(TextFormat.YELLOW + "プレイ時間ランキング"); break;
                default: return;
            }

            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate(exec_sql));
            ResultSet rs = api.getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                int i = 1;
                while(rs.next()){
                    String name = rs.getString("name");
                    String xname = rs.getString("xname");
                    if (name.length() <= 0) name = xname;
                    name = String.format("%2d位: %s", i, name);
                    if (i <= 3) {
                        name = TextFormat.GOLD + name;
                    }
                    objective.registerScore(""+i, "", 0);
                    objective.setScoreText(""+i, name);
                    objective.setScore(""+i, rs.getInt("value"));
                    i++;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            scoreboard.player = player;
            scoreboard.onUpdate(ScoreboardObjective.Descending);

        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return;
        }
    }
}
