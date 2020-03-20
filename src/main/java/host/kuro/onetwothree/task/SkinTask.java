package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.SerializedImage;
import host.kuro.onetwothree.OneTwoThreeAPI;
import utils.DateUtil;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SkinTask extends Thread {

    private OneTwoThreeAPI api;
    private Player player;

    public SkinTask(OneTwoThreeAPI api, Player player) {
        this.api = api;
        this.player = player;
    }

    public void run() {
        try {
            // pathがない場合は DataFoloderへ
            String path = api.getConfig().getString("Web.DataPath");
            File dir = new File(path);
            if (!dir.exists()) {
                path = api.getDataFolderPath() + "/";
            }
            if (dir != null) {
                dir = null;
            }

            if (path.length() <= 0) return;
            path += "skin/" + player.getDisplayName().toLowerCase() + ".png";
            File f = new File(path);
            if (f.exists()) {
                long elapsed = DateUtil.FileDaysElapsed(path);
                int days = api.getConfig().getInt("Web.SkinDays");
                if (elapsed < days) {
                    // 経過日数以内は再作成しない
                    if (f != null) {
                        f = null;
                    }
                    return;
                }
            }

            // スキンデータ取得
            Skin skin = player.getSkin();
            SerializedImage skinData = skin.getSkinData();
            BufferedImage skinBuffer = new BufferedImage(64, (skinData.data.length / 4) / 64, BufferedImage.TYPE_INT_ARGB);
            int index;
            for(int x = 0; x < skinBuffer.getWidth(); x++){
                for(int y = 0; y < skinBuffer.getHeight(); y++){
                    index = (x + y * 64) * 4;
                    skinBuffer.setRGB(x, y, new Color(skinData.data[index] & 0xFF, skinData.data[index + 1] & 0xFF, skinData.data[index + 2] & 0xFF, skinData.data[index + 3] & 0xFF).getRGB());
                }
            }

            // スキンデータ保存
            if(skinBuffer != null){
                ImageIO.write(skinBuffer, "png", f);
            }
            skinBuffer = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
