package dbdrv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.driver.*;
import oracle.sql.*;
import java.util.*;


public class DBAccess {

    private String dburl = null;
    private String user = null;
    private String pswd = null;
    private Connection conn = null;

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

    public String getCompList(int corrTime, int offsetTime) {
        String str = "null";
	try {
            PreparedStatement st = conn.prepareStatement("(SELECT DISTINCT re.compid AS comp "+
                                                         "FROM request re "+
                                                         "WHERE re.begin >= TRUNC(SYSDATE,'dd') "+
                                                         "AND re.begin_use IS NOT NULL "+
                                                         "AND re.status = 1 "+
                                                         "AND sysdate BETWEEN re.begin AND NVL(re.end_use+0.000011574*"+Integer.toString(offsetTime)+",re.end-0.000011574*"+Integer.toString(corrTime)+")) "+
                                                         "UNION "+
                                                         "(SELECT co.compid AS comp FROM comp co WHERE co.logon = 1)");
            ResultSet rs = st.executeQuery();
            int i = 0;
            str = ":";
	    while (rs.next()) {
                str = str+rs.getString("comp")+":";
	    }
    	    rs.close();
	    st.close();
        }
	catch( Exception e ) {
	    e.printStackTrace();
	}
        return str;
    }

}
