package host.kuro.onetwothree.database;

import cn.nukkit.utils.Config;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseManager {
    private OneTwoThreeAPI api = null;
    private String shost;
    private String sPort;
    private String sDb;
    private String sUser;
    private String sPass;
    private Connection conn = null;

    public static final int ERR = -1;
    public static final int DUPLICATE = -2;

    public DatabaseManager(OneTwoThreeAPI api) {
        this.api = api;
        Config cfg = api.getConfig();
        shost = cfg.getString("Database.Host");
        sPort = cfg.getString("Database.Port");
        sDb   = cfg.getString("Database.Database");
        sUser = cfg.getString("Database.User");
        sPass = cfg.getString("Database.Pass");
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean Connect() {
        try{
            conn = DriverManager.getConnection("jdbc:postgresql://" + shost + ":" + sPort + "/" + sDb, sUser, sPass);
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        api.getServer().getLogger().info(Language.translate("onetwothree.dbconnect"));
        return true;
    }

    public boolean DisConnect() {
        try{
            if(conn != null) {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        api.getServer().getLogger().info(Language.translate("onetwothree.dbdisconnect"));
        return true;
    }

    public int ExecuteUpdate(String sql, ArrayList<DatabaseArgs> args) {
        int num = ERR;
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            int i = 1;
            for(Iterator<DatabaseArgs> itr = args.iterator(); itr.hasNext();){
                DatabaseArgs arg = itr.next();
                switch (arg.type) {
                    case "c":
                        ps.setString(i, arg.value);
                        break;
                    case "i":
                        ps.setInt(i, Integer.parseInt(arg.value));
                        break;
                    case"t":
                        ps.setString(i, arg.value);
                        break;
                    case "b":
                        if (arg.value.toLowerCase().equals("true")) {
                            ps.setBoolean(i, true);
                        } else {
                            ps.setBoolean(i, false);
                        }
                        break;
                }
                i++;
            }
            num = ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (e.getSQLState().equalsIgnoreCase("23505")) {
                // 重複エラー
                num = DUPLICATE;
            } else {
                api.getServer().getLogger().info(Language.translate("onetwothree.dberror"));
                org.postgresql.jdbc.PgStatement stmt = (org.postgresql.jdbc.PgStatement)ps;
                this.api.getServer().getLogger().error("SQL : " + stmt.toString());
                this.api.getServer().getLogger().error("ERR_CD : " + e.getSQLState());
                this.api.getServer().getLogger().error("ERR : " + e.getMessage());
                e.printStackTrace();
            }
            try {
                conn.rollback();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e) {
            api.getServer().getLogger().info(Language.translate("onetwothree.dberror"));
            org.postgresql.jdbc.PgStatement stmt = (org.postgresql.jdbc.PgStatement)ps;
            this.api.getServer().getLogger().error("SQL : " + stmt.toString());
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return num;
    }

    public ResultSet ExecuteQuery(PreparedStatement ps, ArrayList<DatabaseArgs> args) {
        ResultSet rs = null;
        try {
            int i = 1;
            if (args != null) {
                for(Iterator<DatabaseArgs> itr = args.iterator(); itr.hasNext();){
                    DatabaseArgs arg = itr.next();
                    switch (arg.type) {
                        case "c":
                            ps.setString(i, arg.value);
                            break;
                        case "i":
                            ps.setInt(i, Integer.parseInt(arg.value));
                            break;
                        case"t":
                            ps.setString(i, arg.value);
                            break;
                    }
                    i++;
                }
            }
            rs = ps.executeQuery();
        } catch (SQLException e) {
            api.getServer().getLogger().info(Language.translate("onetwothree.dberror"));
            org.postgresql.jdbc.PgStatement stmt = (org.postgresql.jdbc.PgStatement)ps;
            this.api.getServer().getLogger().error("SQL : " + stmt.toString());
            this.api.getServer().getLogger().error("ERR_CD : " + e.getSQLState());
            this.api.getServer().getLogger().error("ERR : " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            api.getServer().getLogger().info(Language.translate("onetwothree.dberror"));
            org.postgresql.jdbc.PgStatement stmt = (org.postgresql.jdbc.PgStatement)ps;
            this.api.getServer().getLogger().error("SQL : " + stmt.toString());
            e.printStackTrace();
            return null;
        }
        return rs;
    }
}
