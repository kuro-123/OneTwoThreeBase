package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.SerializedImage;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.utils.DateUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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

            // 2Dスキン保存対応(WEB PHP必須)
            String make_path = api.getConfig().getString("Web.SkinMake");
            if (make_path.length() > 0) {
                HttpURLConnection con = null;
                StringBuffer result = new StringBuffer();
                URL url = new URL(make_path + "?show=body&side=front&cloak=hd&file_name="+player.getDisplayName().toLowerCase());
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                if (con != null) {
                    final int status = con.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        final InputStream in = con.getInputStream();
                        String encoding = con.getContentEncoding();
                        if(null == encoding){
                            encoding = "UTF-8";
                        }
                        final InputStreamReader inReader = new InputStreamReader(in, encoding);
                        final BufferedReader bufReader = new BufferedReader(inReader);
                        String line = null;
                        while((line = bufReader.readLine()) != null) {
                            result.append(line);
                        }
                        bufReader.close();
                        inReader.close();
                        in.close();
                    }
                    con.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
