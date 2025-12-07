package mochineko.java_discord_bot.manager;

import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.FileType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;

public class SQLManager {

    private static final ConfigJson configJson = (ConfigJson) new JsonManager(FileType.CONFIG).getDeserializedJson();

    //SQL
    private Connection con;

    //データベース情報
    private static String DRIVER_NAME; //ドライバーの名前
    private static String DB_HOST; //ホスト（IP）
    private static String DB_PORT; //ポート
    private static String JDBC_URL; //接続したいURL
    private static String USER_ID; //ログインしたいID
    private static String USER_PASS; //ログインしたいユーザーパスワード

    public SQLManager() {
        ConfigJson.DataBase dataBase = configJson.getDatabaseSetting();
        DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
        DB_HOST = dataBase.getHost();
        DB_PORT = String.valueOf(dataBase.getPort());
        JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + dataBase.getDatabaseName() + "?characterEncoding=UTF-8&serverTimezone=Asia/Tokyo";
        USER_ID = dataBase.getUserID();
        USER_PASS = dataBase.getUserPassword();

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnect() {
        if (con != null) {
            return con;
        }
        else {
            try {
                con = DriverManager.getConnection(JDBC_URL, USER_ID, USER_PASS);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return con;
    }

    public Object[] getName(String get_name) {
        try (PreparedStatement stmt = getConnect().prepareStatement("SELECT * FROM users")){
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                long id = rs.getLong("discordID");
                Date date = rs.getDate("joinDate");
                if (name.equalsIgnoreCase(get_name)) {
                    Object[] objects = new Object[5];
                    objects[0] = name;
                    objects[1] = id;
                    objects[2] = date;
                    return objects;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int addName(Member member) {
        try {
            PreparedStatement checkStmt = getConnect().prepareStatement(
                    "SELECT * FROM users WHERE name = (?)"
            );

            checkStmt.setString(1, member.getUser().getName());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                //すでに登録済み
                return -1;
            }

            PreparedStatement playerStmt = getConnect().prepareStatement(
                    "INSERT IGNORE INTO users (name,discordID,joinDate) VALUES (?,?,?)"
            );
            playerStmt.setString(1, member.getUser().getName());
            playerStmt.setBigDecimal(2, new BigDecimal(member.getId()));
            playerStmt.setDate(3, Date.valueOf(member.getTimeJoined().toLocalDate()));
            playerStmt.executeUpdate();

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }}

}
