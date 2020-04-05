package host.kuro.onetwothree;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.command.CommandManager;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.task.RebootTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;

public class BasePlugin extends PluginBase {

    private OneTwoThreeAPI api;
    private TwitterPlugin twitter;
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
        InitDirectory("");
        InitDirectory("skin");
        // API
        this.api = new OneTwoThreeAPI(this);
        // DB
        this.api.getDB().Connect();
        // CMD
        CommandManager.registerAll(this.api);
        // EVENT
        this.getServer().getPluginManager().registerEvents(new EventListener(this.api), this);
        // IP表示
        this.getLogger().info("IP: " + this.api.GetIpInfo());
        // アイテムデータセットアップ
        Calendar cal = Calendar.getInstance();
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY) {
            this.getLogger().info(Language.translate("onetwothree.datasetup"));
            api.SetupNukkitItems();
        }
        if (!getDebug()) {
            // TwitterPlugin
            twitter = new TwitterPlugin(this, api);
            // DiscordPlugin
            try {
                jda = new JDABuilder(config.getString("Discord.botToken")).build();
                jda.awaitReady();
                channelId = config.getString("Discord.channelId");
                ban_channelId = config.getString("Discord.ban_channelId");
                jda.addEventListener(new DiscordChatListener(api));
                if (!config.getString("botStatus").isEmpty()) {
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.DEFAULT, config.getString("botStatus")));
                }
                api.sendDiscordGreenMessage("【123鯖情報】 起動しました！");
            } catch (InterruptedException | LoginException e) {
                e.printStackTrace();
            }
        }
        // 起動
        this.getLogger().info(Language.translate("onetwothree.loaded"));
    }
    public TwitterPlugin getTwitter() {
        return twitter;
    }

    @Override
    public void onDisable() {
        // ステータスクリア
        if (!debug) {
            api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0044"), null);
        }
        this.api.getDB().DisConnect();
        OneTwoThreeAPI.mode.clear();
        OneTwoThreeAPI.item_info.clear();
        OneTwoThreeAPI.player_list.clear();
        this.getLogger().info(Language.translate("onetwothree.unloaded"));
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
            e.printStackTrace();
            api.getLogErr().Write(null, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), "");
        }
        return true;
    }
}