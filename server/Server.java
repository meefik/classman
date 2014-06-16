import java.io.*;
import java.util.*;
import dbdrv.*;


public class Server {

    private String STARTUP_DIR;
    private String OUTPUT_FILE;
    private String EXEC_SCRIPT;
    private int REFRESH_TIME;
    private int CORRECTION_TIME;
    private DBAccess dba;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        String str;
        Random randomGenerator = new Random();
        try {
            readConfig("de_shell.conf");
    	} catch(Exception e) {
    	    e.printStackTrace();
    	}
        while(true) {
    	    try {
                dba.connect();
                str = dba.getCompList(CORRECTION_TIME,REFRESH_TIME);
                dba.disconnect();
                //strToFile(str,OUTPUT_FILE);
                if (str != "null") {
                    shellExec("server "+str);
                }
		Thread.sleep(REFRESH_TIME*1000+randomGenerator.nextInt(REFRESH_TIME*100));
    	    } catch(Exception e) {
    		e.printStackTrace();
    	    }
        }
    }

    public void readConfig(String conf) throws Exception {
        Properties prop = System.getProperties();
        STARTUP_DIR = prop.getProperty("user.dir")+prop.getProperty("file.separator");

        prop = new Properties();
        FileInputStream fin1 = new FileInputStream(STARTUP_DIR+conf);
        prop.load(fin1);
        fin1.close();

        REFRESH_TIME = Integer.valueOf(prop.getProperty("refresh_time")).intValue();
        CORRECTION_TIME = Integer.valueOf(prop.getProperty("correction_time")).intValue();
        OUTPUT_FILE = prop.getProperty("output_file");
        EXEC_SCRIPT = STARTUP_DIR+prop.getProperty("exec_script");

        dba = new DBAccess(prop.getProperty("db_url"), prop.getProperty("db_user"), prop.getProperty("db_password"));
    }

    public void shellExec(String args) throws Exception {
        Process proc = Runtime.getRuntime().exec(EXEC_SCRIPT+" "+args);
        proc.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) { System.out.println("manager.sh: " + line); }
        reader.close();
        proc.getInputStream().close();
        proc.getOutputStream().close();
        proc.getErrorStream().close(); 
        proc.destroy();
    }

    public void strToFile(String[] str, String fname) throws Exception {
        DataOutputStream fout = new DataOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(fname)));
        for (int i = 0; i < str.length; i++) {
            if (str[i] != null) {
                fout.writeBytes(str[i]+"\n");
            }
        }
        fout.close();
    }

}

