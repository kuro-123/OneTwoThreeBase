package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.command.CommandManager;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.scoreboard.Criteria;
import host.kuro.onetwothree.scoreboard.DisplaySlot;
import host.kuro.onetwothree.scoreboard.Scoreboard;
import host.kuro.onetwothree.scoreboard.ScoreboardObjective;
import host.kuro.onetwothree.task.ScoreTask;
import host.kuro.onetwothree.task.TimingTask;
import host.kuro.onetwothree.utils.Particle;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class BasePlugin extends PluginBase {

    private OneTwoThreeAPI api;
    private TwitterPlugin twitter;
    private NpcPlugin npc;
    private boolean debug;
    private JDA jda;
    private String channelId;
    private String ban_channelId;

    public JDA getJDA() { return jda; }
    public String getChannelID() { return channelId; }
    public String getBanChannelID() { return ban_channelId; }
    public boolean getDebug() { return debug; }

    @Override
    public void onEnable() {
        debug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

        this.getDataFolder().mkdirs();
        Language.load(this.getServer().getLanguage().getLang());

        // CONFIG
        saveDefaultConfig();
        Config config = getConfig();

        // 初期ディレクトリチェック
        this.getLogger().info(Language.translate("onetwothree.init_directory"));
        InitDirectory("");
        InitDirectory("skin");
        // API
        this.getLogger().info(Language.translate("onetwothree.init_api"));
        this.api = new OneTwoThreeAPI(this);
        // DB
        this.getLogger().info(Language.translate("onetwothree.init_database"));
        this.api.getDB().Connect();
        // CMD
        this.getLogger().info(Language.translate("onetwothree.init_command"));
        CommandManager.registerAll(this.api);
        // EVENT
        this.getLogger().info(Language.translate("onetwothree.init_event"));
        this.getServer().getPluginManager().registerEvents(new EventListener(this.api), this);
        // IP表示
        this.getLogger().info(Language.translate("onetwothree.disp_ip") + " " + this.api.GetIpInfo());
        // アイテムデータセットアップ
        this.getLogger().info(Language.translate("onetwothree.init_item"));
        api.SetupNukkitItems();
        // ゾーンデータセットアップ
        this.getLogger().info(Language.translate("onetwothree.init_zone"));
        api.SetupZoneInfo();
        // NPCプラグイン
        this.getLogger().info(Language.translate("onetwothree.init_npc"));
        npc = new NpcPlugin(this.api);
        if (!getDebug()) {
            try {
                // Twitterプラグイン
                this.getLogger().info(Language.translate("onetwothree.init_twitter"));
                twitter = new TwitterPlugin(this, api);

                // Discordプラグイン
                this.getLogger().info(Language.translate("onetwothree.init_discord"));
                jda = new JDABuilder(config.getString("Discord.botToken")).build();
                jda.awaitReady();
                channelId = config.getString("Discord.channelId");
                ban_channelId = config.getString("Discord.ban_channelId");
                jda.addEventListener(new DiscordChatListener(api));
                if (!config.getString("botStatus").isEmpty()) {
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.DEFAULT, config.getString("botStatus")));
                }
                api.getMessage().SendDiscordBlueMessage("【123鯖情報】 起動しました！");
            } catch (InterruptedException | LoginException e) {
                api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
            }
        }

        this.getLogger().info(Language.translate("onetwothree.init_world"));
        for (Level lv : api.getServer().getLevels().values()) {
            if (lv.getName().indexOf("nature") >= 0) {
                lv.setSpawnLocation(lv.getSafeSpawn());
            }
        }
        InitWorld();

        this.getLogger().info(Language.translate("onetwothree.init_end"));
        // タイミングタスク起動
        TimingTask task = new TimingTask(api);
        api.getServer().getScheduler().scheduleRepeatingTask(task, 20 * 60 * 60);
        // パーティクル設定
        Particle.SetAPI(api);

        // スコアボードタスク起動
        api.getServer().getScheduler().scheduleRepeatingTask(new ScoreTask(api), 20);

        // 起動
        this.getLogger().info(Language.translate("onetwothree.loaded"));
    }
    public TwitterPlugin getTwitter() {
        return twitter;
    }
    public NpcPlugin getNpc() {
        return npc;
    }

    @Override
    public void onDisable() {
        if (!debug) {
            // ステータスクリア
            this.getLogger().info(Language.translate("onetwothree.unload_status"));
            api.getDB().ExecuteUpdate(Language.translate("Sql0044"), null);
        }

        this.getLogger().info(Language.translate("onetwothree.unload_database"));
        this.api.getDB().DisConnect();

        this.getLogger().info(Language.translate("onetwothree.unload_memory"));
        OneTwoThreeAPI.mode.clear();
        OneTwoThreeAPI.item_info.clear();
        OneTwoThreeAPI.player_list.clear();
        OneTwoThreeAPI.npc_info.clear();
        OneTwoThreeAPI.world_info.clear();
        api.play_time.clear();

        this.getLogger().info(Language.translate("onetwothree.unloaded"));
    }

    private boolean InitWorld() {
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0047"));
            for (Level lv : api.getServer().getLevels().values()) {
                String levelname = lv.getName();
                WorldInfo worldinfo = new WorldInfo();

                ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                args.add(new DatabaseArgs("c", levelname));
                ResultSet rs = api.getDB().ExecuteQuery(ps, args);
                args.clear();
                args = null;
                if (rs != null) {
                    while (rs.next()) {
                        worldinfo.master = rs.getBoolean("master");
                        worldinfo.manager01 = rs.getString("manager01");
                        worldinfo.manager02 = rs.getString("manager02");
                        worldinfo.manager03 = rs.getString("manager03");
                        worldinfo.zone = rs.getBoolean("zone");
                        worldinfo.viewdistance = rs.getInt("viewdistance");
                        worldinfo.splitchat = rs.getBoolean("splitchat");
                        worldinfo.hunger_speed = rs.getInt("hunger_speed");
                        worldinfo.survival = rs.getBoolean("survival");
                        worldinfo.creative = rs.getBoolean("creative");
                        worldinfo.spectator = rs.getBoolean("spectator");
                        worldinfo.adventure = rs.getBoolean("adventure");
                        worldinfo.fly = rs.getBoolean("fly");
                        worldinfo.bbreak = rs.getBoolean("break");
                        worldinfo.bplace = rs.getBoolean("place");
                        worldinfo.pvp = rs.getBoolean("pvp");
                        worldinfo.tagitem = rs.getBoolean("tagitem");
                        worldinfo.crafting = rs.getBoolean("crafting");
                        worldinfo.bed = rs.getBoolean("bed");
                        worldinfo.effect = rs.getBoolean("effect");
                        worldinfo.enchant = rs.getBoolean("enchant");
                        worldinfo.mob = rs.getBoolean("mob");
                        worldinfo.boss = rs.getBoolean("boss");
                        worldinfo.updater = rs.getString("updater");
                        break;
                    }
                }
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                api.world_info.put(levelname, worldinfo);
                lv.gameRules.setGameRule(GameRule.SHOW_COORDINATES, true);
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }

        } catch (Exception e) {
            api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }

    private boolean InitDirectory(String opt) {
        String path;
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.toLowerCase().indexOf("windows") >= 0) {
                path = getDataFolder().getPath() + "/";
            } else {
                path = getConfig().getString("Web.DataPath");
                if (path.length() <= 0) {
                    path = getDataFolder().getPath() + "/";
                }
            }
            if (opt.length() > 0) {
                path += opt + "/";
            }
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
                f.setExecutable(true);
                f.setWritable(true);
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }
}