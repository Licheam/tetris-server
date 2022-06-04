import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.*;
import java.nio.charset.StandardCharsets;

public class DBInfo implements Serializable {
    String JDBC_DRIVER;
    String DB_URL;
    String USER;
    String PASS;
    Connection conn = null;
    Statement stmt = null;
    private static byte[] getSHA(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    private static String toHexString(byte[] hash){
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString());
        while(hexString.length() < 64) hexString.insert(0, "0");
        return hexString.toString();
    }
    private static String encrypt(String password) throws Exception{
        byte[] byteSHAHash = getSHA(password);
        return toHexString(byteSHAHash);
    }

    public DBInfo(String JDBC_DRIVER, String DB_URL, String USER, String PASS){
        this.JDBC_DRIVER = JDBC_DRIVER;
        this.DB_URL = DB_URL;
        this.USER = USER;
        this.PASS = PASS;
        try{
            link();
            create();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public Statement link() throws Exception{
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to a selected database...");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Connected database successfully...");
        stmt = conn.createStatement();
        return stmt;
    }
    public Statement create() throws Exception{
        Class.forName(JDBC_DRIVER);
        System.out.println("Creating database...");
        stmt.execute("CREATE DATABASE IF NOT EXISTS TETRIS");
        stmt.execute("USE TETRIS");
        stmt.execute("CREATE TABLE IF NOT EXISTS `USER`(ID VARCHAR(30) NOT NULL,PASSWORD VARCHAR(100) NOT NULL,PRIMARY KEY(ID));");
        System.out.println("Created database.");
        return stmt;
    }
    public boolean query(String username, String password){
        try {
            String hashPassword = encrypt(password);
            boolean execResult = stmt.execute("SELECT PASSWORD FROM USER WHERE ID=" + username), res = false;
            if (execResult) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    res = hashPassword.equals(rs.getString("PASSWORD"));
                } else {
                    String exec = "INSERT INTO USER (ID, PASSWORD)\nVALUES\n(\"" + username + "\",\"" + hashPassword + "\");";
                    stmt.execute(exec);
                    res = true;
                }
                rs.close();
            }
            return res;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void close() throws Exception{
        stmt.close();
        conn.close();
    }
}
