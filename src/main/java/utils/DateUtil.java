package utils;

import java.io.File;
import java.util.Date;

public class DateUtil {

    // ファイル経過日数を取得
    public static final long FileDaysElapsed(String path) {
        File f = null;
        Date dt = null;
        long ret = -1;
        try {
            f = new File(path);
            Long stamp = f.lastModified();
            dt = new Date();
            ret = (dt.getTime() - stamp) / (1000 * 60 * 60 * 24);
        } catch(Exception e){
        }
        if (f != null) {
            f = null;
        }
        if (f != null) {
            f = null;
        }
        return ret;
    }
}
