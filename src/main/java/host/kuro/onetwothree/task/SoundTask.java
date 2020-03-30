package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.StopSoundPacket;
import host.kuro.onetwothree.OneTwoThreeAPI;

public class SoundTask extends Thread {

    // サウンド一覧（リソースパック内 sound_definitions設定）
    public static final String bgm007 = "music.bgm007"; // ガルちゃん
    public static final String jin001 = "music.jin001"; // ブブー
    public static final String jin002 = "music.jin002"; // ボンッ！
    public static final String jin003 = "music.jin003"; // ドアクローズ
    public static final String jin004 = "music.jin004"; // チュドーン！
    public static final String jin005 = "music.jin005"; // フェードイン
    public static final String jin006 = "music.jin006"; // レベルアップ
    public static final String jin007 = "music.jin007"; // FAIL
    public static final String jin008 = "music.jin008"; // SUCCESS
    public static final String jin009 = "music.jin009"; // 斬
    public static final String jin010 = "music.jin010"; // REGIST
    public static final String jin011 = "music.jin011"; // 水滴
    public static final String jin012 = "music.jin012"; // ピンポン
    public static final String jin013 = "music.jin013"; // kuro音
    public static final String jin014 = "music.jin014"; // ドアノック
    public static final String jin015 = "music.jin015"; // INFO
    public static final String jin016 = "music.jin016"; // ハープ
    public static final String jin017 = "music.jin017"; // GET
    public static final String jin018 = "music.jin018"; // ジャン
    public static final String jin019 = "music.jin019"; // 落下
    public static final String jin020 = "music.jin020"; // WARP
    public static final String jin021 = "music.jin021"; // はずれ
    public static final String jin024 = "music.jin024"; // 資源ワールドIN
    public static final String jin025 = "music.jin025"; // 街ワールドIN
    public static final String jin027 = "music.jin027"; // 荷物を置く
    public static final String jin050 = "music.jin050"; // GET2
    public static final String jin052 = "music.jin052"; // アイテムを置く
    public static final String jin053 = "music.jin053"; // クリック1
    public static final String jin054 = "music.jin054"; // ウィンドウ
    public static final String jin055 = "music.jin055"; // GET3
    public static final String jin060 = "music.jin060"; // 爆発
    public static final String jin061 = "music.jin061"; // ドアクローズ
    public static final String jin071 = "music.jin071"; // レジスター
    public static final String voi044 = "music.voi044"; // いらっしゃい(男)
    public static final String voi045 = "music.voi045"; // 買い取れない（男）
    public static final String voi046 = "music.voi046"; // 売れない（男）
    public static final String voi047 = "music.voi047"; // ボーナスGET
    public static final String voi048 = "music.voi048"; // 注文催促（女）
    public static final String voi050 = "music.voi050"; // いらっしゃいませ(女)

    // 再生モード
    public static final int MODE_STOP = 0;      // 停止
    public static final int MODE_PLAYER = 1;    // プレイヤーのみ
    public static final int MODE_BROADCAST = 2; // ブロードキャスト

    // メンバ
    private Player player;
    private int mode;
    private String sound;
    private int wait;
    private boolean stop;
    private float volume = 0.9F;

    OneTwoThreeAPI api;

    public SoundTask(OneTwoThreeAPI api, Player player, int mode, String sound, int wait, boolean stop) {
        this.api = api;
        this.player = player;
        this.mode = mode;
        this.sound = sound;
        this.wait = wait;
        this.stop = stop;

        // ボリューム
        try {
            String sVol = api.getConfig().getString("Sound.Volume");
            volume = Float.parseFloat(sVol);

        } catch (Exception e) {
            volume = 0.9F;
            e.printStackTrace();
            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage(), player.getDisplayName());
        }
    }

    public void run() {
        try {
            if (mode == MODE_STOP) {
                ExecuteStopAll();
            }
            // ウェイト設定があれば
            if (wait > 0) api.Sleep(wait);

            switch (mode) {
                case MODE_PLAYER:
                    if (stop) ExecuteStop();
                    ExecutePlayPlayer();
                    break;
                case MODE_BROADCAST:
                    if (stop) ExecuteStop();
                    ExecutePlayBroadcast();
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage(), player.getDisplayName());
        }
    }

    private void ExecuteStopAll() {
        // 停止モード
        StopSoundPacket pks = new StopSoundPacket();
        pks.name = "";
        pks.stopAll = true;
        for (Player p : api.getServer().getInstance().getOnlinePlayers().values()) {
            p.dataPacket(pks);
        }
        pks = null;
    }

    private void ExecuteStop() {
        StopSoundPacket pks = new StopSoundPacket();
        pks.name = "";
        pks.stopAll = true;
        player.dataPacket(pks);
        pks = null;
    }

    private void ExecutePlayPlayer() {
        PlaySoundPacket pk = new PlaySoundPacket();
        pk.name = sound;
        pk.volume = 0.9F;
        pk.pitch = 1.0F;
        pk.x = player.getPosition().getFloorX();
        pk.y = player.getPosition().getFloorY();
        pk.z = player.getPosition().getFloorZ();
        player.dataPacket(pk);
        pk = null;
    }

    private void ExecutePlayBroadcast() {
        PlaySoundPacket pk = new PlaySoundPacket();
        pk.name = sound;
        pk.volume = 0.9F;
        pk.pitch = 1.0F;
        for (Player p : api.getServer().getInstance().getOnlinePlayers().values()) {
            pk.x = p.getPosition().getFloorX();
            pk.y = p.getPosition().getFloorY();
            pk.z = p.getPosition().getFloorZ();
            p.dataPacket(pk);
        }
        pk = null;
    }
}
