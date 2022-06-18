import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.*;
import java.nio.charset.StandardCharsets;

/*
class:  DBInfo
Writer: qypan
Usage:  Store the database information and link to target database in order to get the information about the user.
 */
public class DBInfo implements Serializable {
    String JDBC_DRIVER;
    String DB_URL;
    String USER;
    String PASS;
    Connection conn = null;
    Statement stmt = null;
    /*
    Function: getSHA
    Usage:    Get the 'input' and encrypt it with SHA-256, return the byte[] of result.
     */
    private static byte[] getSHA(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /*
    Function: toHexString
    Usage:    Convert the 'hash' byte array to hex string.
     */
    private static String toHexString(byte[] hash){
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString());
        while(hexString.length() < 64) hexString.insert(0, "0");
        return hexString.toString();
    }

    /*
    Function: encrypt
    Usage:    Encrypt the 'password' with SHA-256 and return the hex string of the result.
     */
    private static String encrypt(String password) throws Exception{
        byte[] byteSHAHash = getSHA(password);
        return toHexString(byteSHAHash);
    }

    /*
    Function: DBInfo
    Usage:    Initialize the class.
    Parameters:
        JDBC_DRIVER: the driver information about the database.
        DB_URL:      the url of target database.
        USER:        the username to log in to the database.
        PASS:        the password to log in to the database.
     */
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

    /*
    Function: link
    Usage:    link to the database and return the statement of the linkage.
     */
    public Statement link() throws Exception{
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to a selected database...");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Connected database successfully...");
        stmt = conn.createStatement();
        return stmt;
    }

    /*
    Function: create
    Usage:    create the database we need.
     */
    public Statement create() throws Exception{
        Class.forName(JDBC_DRIVER);
        System.out.println("Creating database...");
        stmt.execute("CREATE DATABASE IF NOT EXISTS TETRIS");
        stmt.execute("USE TETRIS");
        stmt.execute("CREATE TABLE IF NOT EXISTS `USER`(ID VARCHAR(30) NOT NULL,PASSWORD VARCHAR(100) NOT NULL,PRIMARY KEY(ID));");
        System.out.println("Created database.");
        return stmt;
    }

    /*
    Function: query
    Usage:    1.query if the 'username' exists, create it with 'password' if it doesn't exist.
              2.query if the 'password' of the 'username' is correct if the 'username' exists.
     */
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
