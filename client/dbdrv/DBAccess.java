package dbdrv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.driver.*;
import oracle.sql.*;
import java.util.*;


public class DBAccess {

    private String dburl;
    private String user;
    private String pswd;
    private Connection conn;
    public int REID;
    public String BEGIN_DATE;
    public String END_DATE;

    public DBAccess( String dburl, String user, String pswd ) {
	this.dburl = dburl;
	this.user = user;
	this.pswd = pswd;
    }

    public void connect() {
	try {
	    DriverManager.registerDriver( ( java.sql.Driver ) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance() );
	    conn = DriverManager.getConnection ( dburl, user, pswd );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public void disconnect() {
	try {
	    conn.close();
	    DriverManager.deregisterDriver( ( java.sql.Driver ) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance() );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public void getSessionInfo(String comp, int corr) {
	try {
            PreparedStatement st = conn.prepareStatement("SELECT DISTINCT r.reid AS reid, "+
                                                         "TO_CHAR(r.begin,'YYYY.MM.DD HH24:MI:SS') AS begin_date, "+
                                                         "TO_CHAR(r.end-0.000011574*"+Integer.toString(corr)+",'YYYY.MM.DD HH24:MI:SS') AS end_date "+
                                                         "FROM request r "+
                                                         "WHERE r.begin >= TRUNC(SYSDATE,'dd') "+
                                                         "AND r.begin_use IS NOT NULL "+
                                                         "AND r.status = 1 "+
                                                         "AND sysdate BETWEEN r.begin AND NVL(r.end_use,r.end-0.000011574*"+Integer.toString(corr)+") "+
                                                         "AND r.compid = ?");
            st.setString(1,comp);
            ResultSet rs = st.executeQuery();
	    if (rs.next()) {
                REID = rs.getInt("reid");
                BEGIN_DATE = rs.getString("begin_date");
                END_DATE = rs.getString("end_date");
	    }
    	    rs.close();
	    st.close();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }
    
    public String getSessionType(String comp) {
        String str = null;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT urole FROM comp c WHERE c.logon = 1 AND c.compid = ?");
            st.setString(1,comp);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                str = rs.getString("urole");
            }
            rs.close();
            st.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        return str;
    }

    public void getSessionTime(int corr) {
        try {
            PreparedStatement st = conn.prepareStatement("SELECT TO_CHAR(r.begin,'YYYY.MM.DD HH24:MI:SS') AS begin_date, TO_CHAR(NVL(r.end_use,r.end-0.000011574*"+Integer.toString(corr)+"),'YYYY.MM.DD HH24:MI:SS') AS end_date FROM request r WHERE r.reid = ?");
            st.setInt(1,REID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                BEGIN_DATE = rs.getString("begin_date");
                END_DATE = rs.getString("end_date");
            }
            rs.close();
            st.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setSessionRow(String host) {
        int rez = -1;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT count(*) FROM curr_connection cc WHERE cc.reid = ?");
            st.setInt(1,REID);
            ResultSet rs = st.executeQuery();
            if ( rs.next() ) {
                rez = rs.getInt(1);
            }
            rs.close();
            st.close();

            if ( rez == 0 ) {
                st = conn.prepareStatement("INSERT INTO curr_connection VALUES(?, sysdate, null, null, ?)");
                st.setInt(1,REID);
                st.setString(2,host);
                st.executeUpdate();
                st.close();
            } else {
                st = conn.prepareStatement("UPDATE curr_connection cc SET cc.end = null, cc.ip_addr = ? WHERE cc.reid = ?");
                st.setString(1,host);
                st.setInt(2,REID);
	        st.executeUpdate();
                st.close();
            }
        }
        catch( Exception e )  {
            e.printStackTrace();
        }
    }

    public void setEndTimestamp() {
        try {
            PreparedStatement st = conn.prepareStatement("UPDATE curr_connection SET end = sysdate WHERE reid = ?");
            st.setInt(1,REID);
            st.executeUpdate();
            st.close();
        }
        catch( Exception e )  {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        String str = null;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT cc.message FROM curr_connection cc WHERE cc.reid = ?");
            st.setInt(1,REID);
            ResultSet rs = st.executeQuery();
            if ( rs.next() ) {
                str = rs.getString(1);
            }
            rs.close();
            st.close();
            if ( str != null ) {
                st = conn.prepareStatement("update curr_connection set message=null where reid=?");
                st.setInt(1,REID);
                st.executeUpdate();
                st.close();
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
	return str;
    }

/* temp record
    public String getInfo() {
        String timestamp = "";
        try {
            connect();
            PreparedStatement st = conn.prepareStatement("SELECT r.end FROM request r WHERE rownum=1");
            ResultSet rs = st.executeQuery();
            int i = 0;
            if (rs.next()) {
               	timestamp = rs.getString("end");
            }
            rs.close();
            st.close();
            disconnect();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
	return timestamp;
    }
*/

}
