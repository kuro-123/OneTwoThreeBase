package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.StopSoundPacket;
import host.kuro.onetwothree.OneTwoThreeAPI;

public class SoundTask extends Thread {
    // サウンド一覧（リソースパック内 sound_definitions設定）
    public static final String bgm007 = "music.bgm007";
    public static final String jin001 = "music.jin001";
    public static final String jin002 = "music.jin002";
    public static final String jin003 = "music.jin003";
    public static final String jin004 = "music.jin004";
    public static final String jin005 = "music.jin005";
    public static final String jin006 = "music.jin006";
    public static final String jin007 = "music.jin007";
    public static final String jin008 = "music.jin008";
    public static final String jin009 = "music.jin009";
    public static final String jin010 = "music.jin010";
    public static final String jin011 = "music.jin011";
    public static final String jin012 = "music.jin012";
    public static final String jin013 = "music.jin013";
    public static final String jin014 = "music.jin014";
    public static final String jin015 = "music.jin015";
    public static final String jin016 = "music.jin016";
    public static final String jin017 = "music.jin017";
    public static final String jin018 = "music.jin018";
    public static final String jin019 = "music.jin019";
    public static final String jin020 = "music.jin020";
    public static final String jin021 = "music.jin021";
    public static final String jin024 = "music.jin024";
    public static final String jin025 = "music.jin025";
    public static final String jin027 = "music.jin027";
    public static final String jin050 = "music.jin050";
    public static final String jin052 = "music.jin052";
    public static final String jin053 = "music.jin053";
    public static final String jin054 = "music.jin054";
    public static final String jin055 = "music.jin055";
    public static final String jin060 = "music.jin060";
    public static final String jin061 = "music.jin061";
    public static final String jin071 = "music.jin071";
    public static final String voi044 = "music.voi044";
    public static final String voi045 = "music.voi045";
    public static final String voi046 = "music.voi046";
    public static final String voi047 = "music.voi047";
    public static final String voi048 = "music.voi048";
    public static final String voi050 = "music.voi050";

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

    OneTwoThreeAPI api;

    public SoundTask(OneTwoThreeAPI api, Player player, int mode, String sound, int wait, boolean stop) {
        this.api = api;
        this.player = player;
        this.mode = mode;
        this.sound = sound;
        this.wait = wait;
        this.stop = stop;
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
            pk.x = player.getPosition().getFloorX();
            pk.y = player.getPosition().getFloorY();
            pk.z = player.getPosition().getFloorZ();
            player.dataPacket(pk);
        }
        pk = null;
    }
}
