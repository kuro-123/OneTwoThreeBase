package host.kuro.onetwothree;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.item.ItemPrice;
import host.kuro.onetwothree.task.SoundTask;
import host.kuro.onetwothree.utils.LogBlock;
import host.kuro.onetwothree.utils.LogCommand;
import host.kuro.onetwothree.utils.LogError;
import host.kuro.onetwothree.utils.LogWindow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.postgresql.util.LruCache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class OneTwoThreeAPI {

    private static OneTwoThreeAPI instance = null;
    private BasePlugin plugin;
    private DatabaseManager db;
    private LogCommand log_cmd;
    private LogWindow log_win;
    private LogError log_err;
    private LogBlock log_block;

    public SimpleDateFormat sdf_ymdhms = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    public SimpleDateFormat sdf_hms = new SimpleDateFormat("HH:mm:ss");
    public final Map<String, Long> play_time = new HashMap<>();
    public final Map<Player, Integer> play_rank = new HashMap<>();
    public static long systemcall_timing = 0;
    public int lag_task_id = -1;

    public OneTwoThreeAPI(BasePlugin plugin) {
        // インスタンス
    	instance = this;
        this.plugin = plugin;
        this.db = new DatabaseManager(instance);
        this.log_cmd = new LogCommand(this);
        this.log_win = new LogWindow(this);
        this.log_err = new LogError(this);
        this.log_block = new LogBlock(this);
    }
    public static OneTwoThreeAPI getInstance() {
        return instance;
    }
    public BasePlugin getPlugin() { return plugin; }
    public Server getServer() { return plugin.getServer(); }
    public DatabaseManager getDB() {
        return this.db;
    }
    public String getDataFolderPath() {
        return this.plugin.getDataFolder().getPath();
    }
    public Config getConfig() {
        return this.plugin.getConfig();
    }
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
    public PluginLogger getLogger() { return this.plugin.getLogger(); }
    public TwitterPlugin getTwitter() { return plugin.getTwitter(); }

    public LogCommand getLogCmd() { return log_cmd; }
    public LogWindow getLogWin() { return log_win; }
    public LogError getLogErr() { return log_err; }
    public LogBlock getLogBlock() { return log_block; }

    // 各種メモリデータ
    public static HashMap<Player, TAP_MODE> mode = new HashMap<>();
    public static HashMap<Integer, ItemPrice> item_price = new HashMap<>();
    public static HashMap<Integer, String> player_list = new HashMap<>();

    // タップモード
    public static enum TAP_MODE {
        MODE_NONE,
        MODE_TOUCH,
        MODE_KUROVIEW,
    };

    // IPアドレスを取得
    public String GetIpInfo() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            return "---.---.---.---";
        }
    }

    // ホスト取得
    public String GetHostInfo(String address) {
        try {
            //オブジェクトを取得
            InetAddress ia = InetAddress.getByName(address);
            String lstrRet = ia.getHostName();
            ia = null;
            return lstrRet;
        } catch (Exception e) {
            return "---.---.---.---";
        }
    }

    // プラグインタイトルメッセージ
    public String GetMessageTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.GREEN);
        sb.append("[");
        sb.append(Language.translate("onetwothree.name"));
        sb.append("] ");
        return new String(sb);
    }

    // 情報メッセージ
    public String GetInfoMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.WHITE);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // 警告メッセージ
    public String GetWarningMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.YELLOW);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // エラーメッセージ
    public String GetErrMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.RED);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // 半角英数チェック
    public boolean isHankakuEisu(String target) {
        target.replace("_", "");    // _はセーフ
        return Pattern.matches("^[0-9a-zA-Z]+$", target);
    }

    // あいまいプレイヤー検索
    public String AmbiguousSearch(String name) {
        String ret = "";
        try {
            Player target = Server.getInstance().getPlayer(name);
            if (target != null) {
                ret = target.getName();
            }
            if (ret.length() <= 0) {
                // データあいまい検索
                PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0042"));
                ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
                pargs.add(new DatabaseArgs("c", name.toLowerCase() + "%"));
                ResultSet rs = getDB().ExecuteQuery(ps, pargs);
                pargs.clear();
                pargs = null;
                if (rs != null) {
                    while (rs.next()) {
                        ret = rs.getString("name");
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
            }
        } catch (Exception e) {
             ret = "";
        }
        return ret;
    }

    // 一定期間スリープする
    public void Sleep(int millsec) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while(true) {
            endTime = System.currentTimeMillis();
            if ((endTime - startTime) > millsec) {
                break;
            }
            Thread.yield();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }

    public boolean IsTagItem(Item item) {
        String cus = item.getCustomName();
        if (cus.length() <= 0) return false;

        String symbol = getConfig().getString("GameSettings.ItemTag");
        if (symbol.length() <= 0) return false;

        if (cus.indexOf(symbol) <= 0) return false;
        return true;
    }

    public TAP_MODE IsTouchmode(Player player) {
        if (!mode.containsKey(player)) {
            return TAP_MODE.MODE_NONE;
        }
        return mode.get(player);
    }

    public String GetBlockInfoMessage(Block block) {
        if (block == null) return "";

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
        return new String(sb);
    }

    public void OpenKuroView(Player player, Block block) {
        if (block == null) return;

        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", block.getLevel().getName()));
            args.add(new DatabaseArgs("i", ""+block.getFloorX()));
            args.add(new DatabaseArgs("i", ""+block.getFloorY()));
            args.add(new DatabaseArgs("i", ""+block.getFloorZ()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0036"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            boolean hit=false;
            if (rs != null) {
                //BL.log_date,UN.name,BL.act,BL.block_name
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
                Timestamp log_date = null;
                String pname = "";
                String act = "";
                String bname = "";
                String bid = "";
                String bmeta = "";
                while(rs.next()){
                    log_date = rs.getTimestamp("log_date");
                    String stime = sdf.format(log_date);
                    pname = rs.getString("name");
                    act = rs.getString("act");
                    act = act.replace("break", "破壊");
                    act = act.replace("place", "建築");
                    bname = rs.getString("block_name");
                    bid = rs.getString("block_id");
                    bmeta = rs.getString("block_meta");

                    sb.append(TextFormat.WHITE); sb.append("日時: ");   sb.append(TextFormat.WHITE); sb.append(stime);
                    sb.append(TextFormat.WHITE); sb.append(" ﾌﾟﾚｲﾔｰ: "); sb.append(TextFormat.WHITE); sb.append(pname);
                    sb.append(TextFormat.WHITE); sb.append("\n動作: ");   sb.append(TextFormat.YELLOW); sb.append(act);
                    sb.append(TextFormat.WHITE); sb.append(" ﾌﾞﾛｯｸ: ");  sb.append(TextFormat.GREEN); sb.append(bname + " (ID:" + bid + " META:" + bmeta + ")");
                    sb.append("\n\n");
                    hit = true;
                }
                if (hit) {
                    // ウィンドウ
                    StringBuilder sb_title = new StringBuilder();
                    sb_title.append("場所: ");
                    sb_title.append(block.getLevel().getName());
                    sb_title.append(" x: ");
                    sb_title.append(block.getFloorX());
                    sb_title.append(", y: ");
                    sb_title.append(block.getFloorY());
                    sb_title.append(", z: ");
                    sb_title.append(block.getFloorZ());
                    sb_title.append(TextFormat.GREEN);
                    sb_title.append("\n履歴順は上からが最新↓\n");
                    sb_title.append(TextFormat.WHITE);
                    PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                    SimpleForm form = new SimpleForm("くろビュー", new String(sb_title) + new String(sb));
                    form.send(player, (targetPlayer, targetForm, data) -> {
                    });
                } else {
                    player.sendMessage(GetWarningMessage("commands.kv.none"));
                    PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
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
        } catch (Exception e) {
            e.printStackTrace();
            getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    // サウンド再生
    public void PlaySound(Player player, int mode, String sound, int wait, boolean stop) {
        SoundTask task = new SoundTask(this, player, mode, sound, wait, stop);
        task.start();
    }

    public void SetupNukkitItems() {
        try {
            for (Class c : Item.list) {
                if (c != null) {
                    String name = c.getSimpleName();
                    name = name.replace("Block", "");
                    name = name.replace("Item", "");
                    Item item = Item.fromString(name);
                    if (item != null) {
                        int id = item.getId();
                        // ステータス更新
                        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                        args.add(new DatabaseArgs("i", ""+id));
                        args.add(new DatabaseArgs("c", name));
                        int ret = getDB().ExecuteUpdate(getConfig().getString("SqlStatement.Sql0022"), args);
                        args.clear();
                        args = null;
                    }
                }
            }
            PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0023"));
            ResultSet rs = getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                while(rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    item_price.put(id, new ItemPrice(id, name, price));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int GetRank(Player player) {
        int ret = -1;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0028"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret = rs.getInt("rank");
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
        } catch (Exception e) {
            e.printStackTrace();
            getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return ret;
    }

    public String GetRankName(Player player) {
        int rank = play_rank.get(player);
        switch (rank) {
            case 0: return "なし";
            case 1: return "一般";
            case 2: return "信任";
            case 3: return "管理";
            case 4: return "鯖主";
        }
        return "なし";
    }
    public String GetRankColor(Player player) {
        if (player == null) {
            return ""+TextFormat.GRAY;
        }
        int rank = play_rank.get(player);
        switch (rank) {
            case 0: return ""+TextFormat.GRAY;
            case 1: return ""+TextFormat.WHITE;
            case 2: return ""+TextFormat.AQUA;
            case 3: return ""+TextFormat.GREEN;
            case 4: return ""+TextFormat.MINECOIN_GOLD;
        }
        return ""+TextFormat.GRAY;
    }

    public boolean IsKanri(Player player) {
        int rank = play_rank.get(player);
        if (rank < 3) {
            return false;
        }
        return true;
    }
    public boolean IsJyumin(Player player) {
        int rank = play_rank.get(player);
        if (rank < 1) {
            return false;
        }
        return true;
    }

    public void sendDiscordMessage(Player player, String message) {
        try {
            JDA jda = getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = sdf_hms.format(new Date());
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
            e.printStackTrace();
            getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }
    public void sendDiscordRedMessage(String message) {
        try {
            JDA jda = getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = sdf_hms.format(new Date());
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
            e.printStackTrace();
        }
    }
    public void sendDiscordBlueMessage(String message) {
        try {
            JDA jda = getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = sdf_hms.format(new Date());
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
            e.printStackTrace();
        }
    }
    public void sendDiscordGreenMessage(String message) {
        try {
            JDA jda = getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = sdf_hms.format(new Date());
                StringBuilder sb = new StringBuilder();
                sb.append("```py\n");
                sb.append("  ");
                sb.append(chat_time);
                sb.append(" [鯖内] ");
                sb.append(CutSection(message));
                sb.append("\n```");
                channel.sendMessage(new String(sb)).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendDiscordYellowMessage(String message) {
        try {
            JDA jda = getPlugin().getJDA();
            if (jda == null) return;
            TextChannel channel = jda.getTextChannelById(getPlugin().getChannelID());
            if (channel != null) {
                String chat_time = sdf_hms.format(new Date());
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
            e.printStackTrace();
        }
    }
    public String CutSection(String message) {
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
}
