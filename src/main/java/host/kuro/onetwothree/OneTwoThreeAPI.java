package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockConcrete;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.datatype.NpcInfo;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.datatype.ZoneInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.task.AfkTask;
import host.kuro.onetwothree.task.SoundTask;
import host.kuro.onetwothree.utils.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class OneTwoThreeAPI {

    private static OneTwoThreeAPI instance = null;
    private static Message mes = null;

    private BasePlugin plugin;
    private DatabaseManager db;
    private LogCommand log_cmd;
    private LogWindow log_win;
    private LogError log_err;
    private LogBlock log_block;
    private LogPay log_pay;
    private LogSign log_sign;

    private static final MtRand random = new MtRand(System.currentTimeMillis());

    // 各種メモリデータ
    public SimpleDateFormat sdf_ymdhms = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    public SimpleDateFormat sdf_hms = new SimpleDateFormat("HH:mm:ss");
    public NumberFormat comma_format = NumberFormat.getNumberInstance();
    public final Map<String, Long> play_time = new HashMap<>();
    public final Map<Player, Integer> play_rank = new HashMap<>();
    public static long systemcall_timing = 0;
    public int lag_task_id = -1;
    public int world_task_id = -1;

    public static HashMap<Player, TAP_MODE> mode = new HashMap<>();
    public static HashMap<Integer, ItemInfo> item_info = new HashMap<>();
    public static HashMap<Integer, String> player_list = new HashMap<>();
    public static HashMap<Player, NpcInfo> npc_info = new HashMap<>();
    public static HashMap<String, WorldInfo> world_info = new HashMap<>();
    public static HashMap<String, ZoneInfo> zone_info = new HashMap<>();

    public static HashMap<Player, Integer> select_seq = new HashMap<>();
    public static HashMap<Player, Location> select_one = new HashMap<>();
    public static HashMap<Player, Location> select_two = new HashMap<>();

    public static HashMap<Player, Long> tip_wait = new HashMap<>();

    public static HashMap<String, String> black_cid = new HashMap<>();
    public static HashMap<String, String> black_xuid = new HashMap<>();

    private AfkTask task_afk = null;

    public OneTwoThreeAPI(BasePlugin plugin) {
        // インスタンス
    	instance = this;

        this.mes = new Message(this);
        this.plugin = plugin;
        this.db = new DatabaseManager(instance);
        this.log_cmd = new LogCommand(this);
        this.log_win = new LogWindow(this);
        this.log_err = new LogError(this);
        this.log_block = new LogBlock(this);
        this.log_pay = new LogPay(this);
        this.log_sign = new LogSign(this);

        task_afk = new AfkTask(this);
        getServer().getScheduler().scheduleRepeatingTask(task_afk, 20 * 60 * 10);
    }
    public static OneTwoThreeAPI getInstance() {
        return instance;
    }
    public static Message getMessage() {
        return mes;
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
    public MtRand getRand() { return random; }
    public boolean getDebug() { return plugin.getDebug(); }
    public AfkTask getAfk() { return task_afk; }

    public LogCommand getLogCmd() { return log_cmd; }
    public LogWindow getLogWin() { return log_win; }
    public LogError getLogErr() { return log_err; }
    public LogBlock getLogBlock() { return log_block; }
    public LogPay getLogPay() { return log_pay; }
    public LogSign getLogSign() { return log_sign; }

    // タップモード
    public static enum TAP_MODE {
        MODE_NONE,
        MODE_TOUCH,
        MODE_KUROVIEW,
        MODE_NPC,
        MODE_ZONE,
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


    // 半角英数チェック
    public boolean isHankakuEisu(String target) {
        target.replace("_", "");    // _はセーフ
        return Pattern.matches("^[0-9a-zA-Z]+$", target);
    }

    // プレイヤー検索
    public Player GetPlayerEx(String name) {
        Player ret = null;
        try {
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0053"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", name.toLowerCase()));
            pargs.add(new DatabaseArgs("c", name.toLowerCase()));
            ResultSet rs = getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    String namebuff = rs.getString("xname");
                    ret = getServer().getPlayerExact(namebuff);
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

        } catch (Exception e) {
            return null;
        }
        return ret;
    }

    public String GetAmbiguousXuid(String name) {
        String ret = "";
        try {
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0060"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", "%" + name.toLowerCase() + "%"));
            pargs.add(new DatabaseArgs("c", "%" + name.toLowerCase() + "%"));
            ResultSet rs = getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    ret = rs.getString("xuid");
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

        } catch (Exception e) {
            return null;
        }
        return ret;
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
                PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0042"));
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

    public void OpenKuroView(Player player, Block block) {
        if (block == null) return;

        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", block.getLevel().getName()));
            args.add(new DatabaseArgs("i", ""+block.getFloorX()));
            args.add(new DatabaseArgs("i", ""+block.getFloorY()));
            args.add(new DatabaseArgs("i", ""+block.getFloorZ()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0036"));
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
                    getMessage().SendWarningMessage(Language.translate("commands.kv.none"), player);
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
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
    }

    // サウンド再生
    public void PlaySound(Player player, int mode, String sound, int wait, boolean stop) {
        SoundTask task = new SoundTask(this, player, mode, sound, wait, stop);
        task.start();
    }

    public void SetupNukkitItems() {
        try {
            if (getDebug()) {
                Calendar cal = Calendar.getInstance();
                int week = cal.get(Calendar.DAY_OF_WEEK);
                if (week == Calendar.SUNDAY) {
                    for (int i=0; i<=512; i++) {
                        Item item = Item.get(i);
                        if (item == null) continue;
                        String name = item.getName();
                        name = name.replace("Item", "");
                        name = name.replace("item", "");
                        name = name.trim();
                        int id = item.getId();
                        int meta = item.getDamage();
                        if (meta > 0) {
                            int aaaa = 0;
                        }
                        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                        args.add(new DatabaseArgs("i", ""+id));
                        args.add(new DatabaseArgs("c", name));
                        args.add(new DatabaseArgs("c", name));
                        int ret = getDB().ExecuteUpdate(Language.translate("Sql0022"), args);
                        args.clear();
                        args = null;
                    }
                }
            }
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0023"));
            ResultSet rs = getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                while(rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    String ban = rs.getString("ban");
                    item_info.put(id, new ItemInfo(id, name, price, ban));
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
            getLogErr().Write(null, getMessage().GetErrorMessage(e));
        }
    }

    public void SetupZoneInfo() {
        try {
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0055"));
            ResultSet rs = getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                int i = 0;
                while(rs.next()){
                    ZoneInfo zi = new ZoneInfo();
                    zi.level = rs.getString("level");
                    zi.x1 = rs.getInt("x1");
                    zi.z1 = rs.getInt("z1");
                    zi.x2 = rs.getInt("x2");
                    zi.z2 = rs.getInt("z2");
                    zi.rank = rs.getString("rank");
                    zi.gm = rs.getString("gm");
                    zi.name = rs.getString("name");
                    zi.owner = rs.getString("owner");
                    zi.message = rs.getString("message");
                    zi.pub = rs.getBoolean("public");
                    zi.sound = rs.getString("sound");
                    zi.partner01 = rs.getString("partner01");
                    zi.partner02 = rs.getString("partner02");
                    zi.partner03 = rs.getString("partner03");
                    zi.updater = rs.getString("updater");
                    String key = rs.getString("level") + rs.getInt("x1") + rs.getInt("z1") + rs.getInt("x2") + rs.getInt("z2");
                    zone_info.put(key, zi);
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
        } catch (Exception e) {
            getLogErr().Write(null, getMessage().GetErrorMessage(e));
        }
    }

    public int GetRank(Player player) {
        int ret = -1;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0028"));
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
            getLogErr().Write(null, getMessage().GetErrorMessage(e));
        }
        return ret;
    }

    public String GetRankName(Player player) {
        if (play_rank != null) {
            int rank = play_rank.get(player);
            switch (rank) {
                case 0: return "訪問";
                case 1: return "住民";
                case 2: return "ＧＭ";
                case 3: return "パイ";
                case 4: return "鯖主";
            }
        }
        return "なし";
    }
    public String GetRankColor(Player player) {
        try {
            if (player == null) {
                return ""+TextFormat.GRAY;
            }
            if (play_rank != null) {
                int rank = play_rank.get(player);
                switch (rank) {
                    case 0: return ""+TextFormat.GRAY;
                    case 1: return ""+TextFormat.WHITE;
                    case 2: return ""+TextFormat.GREEN;
                    case 3: return ""+TextFormat.GOLD;
                    case 4: return ""+TextFormat.MINECOIN_GOLD;
                }
            }

        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ""+TextFormat.GRAY;
    }

    public boolean IsNushi(Player player) {
        if (player == null) return false;
        if (play_rank != null) {
            if (play_rank.size() == 0) {
                int rank = GetRank(player);
                play_rank.put(player, rank);
            }
            int rank = play_rank.get(player);
            if (rank < 4) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
    public boolean IsGameMaster(Player player) {
        if (player == null) return false;
        try {
            if (play_rank.size() == 0) {
                int rank = GetRank(player);
                play_rank.put(player, rank);
            }
            if (play_rank != null) {
                int rank = play_rank.get(player);
                if (rank < 2) {
                    return false;
                }
            } else {
                return false;
            }
            return true;

        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
            return false;
        }
    }
    public boolean IsJyumin(Player player) {
        if (player == null) return false;
        if (play_rank != null) {
            if (play_rank.size() == 0) {
                int rank = GetRank(player);
                play_rank.put(player, rank);
            }
            int rank = play_rank.get(player);
            if (rank < 1) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
    public boolean IsVisitor(Player player) {
        if (player == null) return false;
        if (play_rank != null) {
            if (play_rank.size() == 0) {
                int rank = GetRank(player);
                play_rank.put(player, rank);
            }
            int rank = play_rank.get(player);
            if (rank == 0) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public boolean IsBanItem(Player player, Item item) {
        if (item == null) return false;
        if (item_info == null) return false;
        if (player == null) return false;

        int id = item.getId();
        ItemInfo ip = null;
        if (item_info.containsKey(id)) {
            ip = item_info.get(id);
        }
        if (ip == null) return false;
        if (ip.ban == null) return false;
        if (!ip.ban.equals("〇")) return false;

        getMessage().SendBanMessage(player, ip.name);
        return true;
    }

    public void SpawnNpc(Player player, Block block) {
        if (!OneTwoThreeAPI.npc_info.containsKey(player)) return;
        NpcInfo npc_info = OneTwoThreeAPI.npc_info.get(player);

        Location loc = block.getLocation();
        loc.y += 1.2F;

        Float yaw =Float.parseFloat(String.valueOf(player.getYaw()));
        Float pitch = Float.parseFloat(String.valueOf(player.getPitch()));

        getPlugin().getNpc().SetNpcSpawn(
            player,
            loc,
            npc_info,
            null,
            null,
            null,
            null,
            null,
            yaw,
            pitch);
    }

    public WorldInfo GetWorldInfo(Player player) {
        if (player == null) return null;
        String levelname = player.getLevel().getName();
        if (!world_info.containsKey(levelname)) return null;
        return world_info.get(levelname);
    }
    public WorldInfo GetWorldInfo(Level level) {
        if (level == null) return null;
        if (!world_info.containsKey(level.getName())) return null;
        return world_info.get(level.getName());
    }

    public int GetChatCount(Player player) {
        int ret = 0;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0049"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret = rs.getInt("chat");
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
        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ret;
    }

    public int GetBreakPlaceCount(Player player) {
        int ret = 0;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0049"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret  = rs.getInt("break");
                    ret += rs.getInt("place");
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
        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ret;
    }

    public int GetMoney(Player player) {
        int ret = -1;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0050"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret = rs.getInt("money");
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
        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ret;
    }
    public boolean PayMoney(Player player, int pay) {
        int ret = -1;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("i", ""+pay));
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ret = getDB().ExecuteUpdate(Language.translate("Sql0051"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
    public boolean AddMoney(Player player, int add) {
        int ret = -1;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("i", ""+add));
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ret = getDB().ExecuteUpdate(Language.translate("Sql0052"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }

    public int Selection(Player player, Location loc) {
        if (select_seq == null) {
            select_seq.put(player, 1);
            select_one.put(player, loc);
            getMessage().SendInfoMessage(Language.translate("onetwothree.selection.one") + " [ x:" + loc.getFloorX() + " z:" + loc.getFloorZ() + " ]", player);
            return 1;
        } else {
            if (!select_seq.containsKey(player)) {
                select_seq.put(player, 1);
                select_one.put(player, loc);
                getMessage().SendInfoMessage(Language.translate("onetwothree.selection.one") + " [ x:" + loc.getFloorX() + " z:" + loc.getFloorZ() + " ]", player);
                return 1;
            } else {
                int seq = select_seq.get(player);
                if (seq == 1) {
                    select_seq.put(player, 2);
                    select_two.put(player, loc);
                    getMessage().SendInfoMessage(Language.translate("onetwothree.selection.two") + " [ x:" + loc.getFloorX() + " z:" + loc.getFloorZ() + " ]", player);
                    return 2;
                } else if (seq == 2) {
                    select_seq.put(player, 1);
                    select_one.put(player, loc);
                    getMessage().SendInfoMessage(Language.translate("onetwothree.selection.one") + " [ x:" + loc.getFloorX() + " z:" + loc.getFloorZ() + " ]", player);
                    return 1;
                }
            }
        }
        select_seq.put(player, 1);
        select_one.put(player, loc);
        getMessage().SendInfoMessage(Language.translate("onetwothree.selection.one") + " [ x:" + loc.getFloorX() + " z:" + loc.getFloorZ() + " ]", player);
        return 1;
    }

    public void SetZoneRank(Player player) {
        // 隣接チェック
        Location loc1 = select_one.get(player);
        if (!IsInside(loc1)) {
            getMessage().SendWarningMessage(Language.translate("onetwothree.zone.err_colision"), player);
            return;
        }
        Location loc2 = select_two.get(player);
        if (!IsInside(loc2)) {
            getMessage().SendWarningMessage(Language.translate("onetwothree.zone.err_colision"), player);
            return;
        }

        // ゾーン設定ウィンドウ
        List<String> zList = new ArrayList<String>();
        zList.add(Language.translate("onetwothree.selection.none"));
        zList.add("土地の価値： Aﾗﾝｸ ﾌﾞﾛｯｸ単価： 960p");
        zList.add("土地の価値： Bﾗﾝｸ ﾌﾞﾛｯｸ単価： 640p");
        zList.add("土地の価値： Cﾗﾝｸ ﾌﾞﾛｯｸ単価： 320p");
        zList.add("土地の価値： Dﾗﾝｸ ﾌﾞﾛｯｸ単価： 160p");
        zList.add("土地の価値： Eﾗﾝｸ ﾌﾞﾛｯｸ単価：  80p");
        zList.add("土地の価値： Fﾗﾝｸ ﾌﾞﾛｯｸ単価：  40p");
        CustomForm form = new CustomForm("ゾーンランク設定")
                .addLabel("指定したゾーンのランクを決めてください")
                .addDropDown("土地ランク設定", zList);
        PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data == null) {
                    getMessage().SendWarningMessage(Language.translate("onetwothree.zone.err_window"), targetPlayer);
                    return;
                }
                // ウィンドウログ
                getLogWin().Write(targetPlayer, Language.translate("ゾーンランク設定"), data.get(1).toString(), "", "", "", "", "", targetPlayer.getDisplayName());

                String rank_name = data.get(1).toString();
                int rank_meta = 0;
                if (rank_name.indexOf("A")>=0) {
                    rank_name = "A";
                    rank_meta = 6;
                } else if (rank_name.indexOf("B")>=0) {
                    rank_name = "B";
                    rank_meta = 4;
                } else if (rank_name.indexOf("C")>=0) {
                    rank_name = "C";
                    rank_meta = 3;
                } else if (rank_name.indexOf("D")>=0) {
                    rank_name = "D";
                    rank_meta = 1;
                } else if (rank_name.indexOf("E")>=0) {
                    rank_name = "E";
                    rank_meta = 0;
                } else if (rank_name.indexOf("F")>=0) {
                    rank_name = "F";
                    rank_meta = 8;
                } else {
                    getMessage().SendWarningMessage(Language.translate("onetwothree.zone.err_choise"), targetPlayer);
                    return;
                }
                Location l1 = select_one.get(targetPlayer);
                Location l2 = select_two.get(targetPlayer);

                ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                args.add(new DatabaseArgs("c", targetPlayer.getLevel().getName()));
                args.add(new DatabaseArgs("i", ""+l1.getFloorX()));
                args.add(new DatabaseArgs("i", ""+l1.getFloorZ()));
                args.add(new DatabaseArgs("i", ""+l2.getFloorX()));
                args.add(new DatabaseArgs("i", ""+l2.getFloorZ()));
                args.add(new DatabaseArgs("c", rank_name));
                args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                int ret = getDB().ExecuteUpdate(Language.translate("Sql0054"), args);
                args.clear();
                args = null;
                if (ret <= 0) {
                    getMessage().SendWarningMessage(Language.translate("onetwothree.zone.err_regist"), targetPlayer);
                    return;
                }

                ZoneInfo zi = new ZoneInfo();
                zi.level = targetPlayer.getLevel().getName();
                zi.x1 = l1.getFloorX();
                zi.z1 = l1.getFloorZ();
                zi.x2 = l2.getFloorX();
                zi.z2 = l2.getFloorZ();
                zi.rank = rank_name;
                zi.gm = targetPlayer.getDisplayName();
                zi.updater = targetPlayer.getDisplayName();
                String key = zi.level+zi.x1+zi.z1+zi.x2+zi.z2;
                zone_info.put(key, zi);

                // 四点にコンクリートを建てる
                int x1, y, z1;
                int x2, z2;
                int x3, z3;
                int x4, z4;
                x1 = loc1.getFloorX(); z1=loc1.getFloorZ(); y = loc1.getFloorY()+1;
                x2 = loc1.getFloorX(); z2=loc2.getFloorZ();
                x3 = loc2.getFloorX(); z3=loc1.getFloorZ();
                x4 = loc2.getFloorX(); z4=loc2.getFloorZ();
                targetPlayer.getLevel().setBlock(new Position(x1, y, z1), new BlockConcrete(rank_meta));
                targetPlayer.getLevel().setBlock(new Position(x2, y, z2), new BlockConcrete(rank_meta));
                targetPlayer.getLevel().setBlock(new Position(x3, y, z3), new BlockConcrete(rank_meta));
                targetPlayer.getLevel().setBlock(new Position(x4, y, z4), new BlockConcrete(rank_meta));

                // ブロードキャスト通知
                getMessage().SendZoneMessage(targetPlayer, rank_name);
                targetPlayer.sendTitle("ゾーン設定完了", TextFormat.RED + rank_name + "ランクゾーン", 10,80, 10);

            } catch (Exception e) {
                getMessage().SendErrorMessage(Language.translate("onetwothree.zone.err_regist"), targetPlayer);
                getLogErr().Write(targetPlayer, getMessage().GetErrorMessage(e));
            }

            OneTwoThreeAPI.mode.put(targetPlayer, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
            OneTwoThreeAPI.select_seq.remove(targetPlayer);
            OneTwoThreeAPI.select_one.remove(targetPlayer);
            OneTwoThreeAPI.select_two.remove(targetPlayer);
        });
    }

    public boolean IsInside(Location target) {
        for (String key : zone_info.keySet()) {
            ZoneInfo zi = zone_info.get(key);
            int minX = Math.min(zi.x1, zi.x2);
            int minZ = Math.min(zi.z1, zi.z2);
            int maxX = Math.max(zi.x1, zi.x2);
            int maxZ = Math.max(zi.z1, zi.z2);
            if (!zi.level.equals(target.getLevel().getName())) continue;
            if (minX > target.getFloorX()) continue;
            if (maxX < target.getFloorX()) continue;
            if (minZ > target.getFloorZ()) continue;
            if (maxZ < target.getFloorZ()) continue;
            return false;
        }
        return true;
    }
    public ZoneInfo IsInsideInfo(Location target) {
        for (String key : zone_info.keySet()) {
            ZoneInfo zi = zone_info.get(key);
            int minX = Math.min(zi.x1, zi.x2);
            int minZ = Math.min(zi.z1, zi.z2);
            int maxX = Math.max(zi.x1, zi.x2);
            int maxZ = Math.max(zi.z1, zi.z2);
            if (!zi.level.equals(target.getLevel().getName())) continue;
            if (minX > target.getFloorX()) continue;
            if (maxX < target.getFloorX()) continue;
            if (minZ > target.getFloorZ()) continue;
            if (maxZ < target.getFloorZ()) continue;
            return zi;
        }
        return null;
    }

    public void RandomPresent(Player player, String symbol) {
        int value = getRand().Next(0, 10000);
        if (value != 1) return;

        int count = 0;
        while(count<50) {
            count++;
            int rand_id = getRand().Next(1, 512);

            if (!item_info.containsKey(rand_id)) continue;
            ItemInfo ii = item_info.get(rand_id);

            if (ii.ban.equals("1")) continue;
            if (ii.price <= 0) continue;

            Item item = Item.get(ii.id, 0 , 1);
            item.setCustomName(item.getName()+symbol);

            Location loc = player.getLocation();
            int x = loc.getFloorX() + getRand().Next(-2, 2);
            int y = loc.getFloorY() + getRand().Next( 0, 2);
            int z = loc.getFloorZ() + getRand().Next(-2, 2);
            player.getLevel().dropItem(new Vector3(x, y, z), item);

            getMessage().SendDropMessage(player, item.getName());
            return;
        }
    }

    public boolean CheckCid(Player player) {
        boolean ret = true;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", ""+player.getLoginChainData().getClientId()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0065"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret = false;
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
        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ret;
    }

    public boolean CheckXuid(Player player) {
        boolean ret = true;
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            PreparedStatement ps = getDB().getConnection().prepareStatement(Language.translate("Sql0066"));
            ResultSet rs = getDB().ExecuteQuery(ps, args);
            args.clear();
            args = null;
            if (rs != null) {
                while(rs.next()){
                    ret = false;
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
        } catch (Exception e) {
            getLogErr().Write(player, getMessage().GetErrorMessage(e));
        }
        return ret;
    }
}
