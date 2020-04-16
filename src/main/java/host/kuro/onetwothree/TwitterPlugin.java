package host.kuro.onetwothree;

import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.task.SoundTask;
import host.kuro.onetwothree.task.TwitterTask;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterPlugin {
    // メンバー
    private BasePlugin plugin;
    private OneTwoThreeAPI api;
    private Twitter twitter = null;

    public TwitterPlugin(BasePlugin plugin, OneTwoThreeAPI api) {
        this.plugin = plugin;
        this.api = api;
        Register();
    }

    private boolean Register() {
        try {
            final String TWITTER_CONSUMER_KEY = plugin.getConfig().getString("Twitter.ConsumerKey");
            final String TWITTER_CONSUMER_SECRET = plugin.getConfig().getString("Twitter.ConsumerSecret");
            final String TWITTER_ACCESS_TOKEN = plugin.getConfig().getString("Twitter.AccessToken");
            final String TWITTER_ACCESS_TOKEN_SECRET = plugin.getConfig().getString("Twitter.AccessTokenSecret");
            final String TWITTER_BROADCAST_ID = plugin.getConfig().getString("Twitter.BroadcastID");

            if (TWITTER_CONSUMER_KEY.length() <= 0 ||
                TWITTER_CONSUMER_SECRET.length() <= 0 ||
                TWITTER_ACCESS_TOKEN.length() <= 0 ||
                TWITTER_ACCESS_TOKEN_SECRET.length() <= 0 ||
                TWITTER_BROADCAST_ID.length() <= 0) {
                return false;
            }

            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            builder.setOAuthAccessToken(TWITTER_ACCESS_TOKEN);
            builder.setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);
            Configuration conf = builder.build();

            // TwitterStreamのインスタンス作成
            TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
            // Listenerを登録
            twitterStream.addListener(new TweetListener(plugin, api));
            //フィルターを設定する
            FilterQuery filter = new FilterQuery();
            long val = Long.parseLong(TWITTER_BROADCAST_ID);
            filter.follow(new long[]{val});
            twitterStream.filter(filter);

            // ツイッタータスク起動
            twitter = new TwitterFactory(conf).getInstance();
            plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, new TwitterTask(plugin, api, twitter), 20*60*60); // 1時間おき

        } catch (Exception e) {
            api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }

    public boolean Tweet(String message) {
        try {
            twitter.updateStatus(message);

        } catch (Exception e) {
            api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }
}

/** Tweetを出力するだけのListener */
class TweetListener extends StatusAdapter {
    private BasePlugin plugin;
    private OneTwoThreeAPI api;
    public TweetListener(BasePlugin plugin, OneTwoThreeAPI api) {
        this.plugin = plugin;
        this.api = api;
    }
    public void onStatus(Status status) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.GOLD);
        sb.append(TextFormat.BOLD);
        sb.append("[" + Language.translate("twitter.title") + "] : ");
        String mes = status.getText();
        mes = mes.replace("\r\n", "\n");
        mes = mes.replace("\n\n", "\n");
        if (mes.length() > 32) {
            mes = mes.substring(0, 32);
        }
        sb.append(mes);
        String message = new String(sb);
        api.getMessage().SendInfoMessage(message, true);
    }
}
