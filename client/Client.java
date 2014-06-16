import java.io.*;
import java.util.*;
import dbdrv.*;
//import javax.swing.SwingUtilities;
import java.text.SimpleDateFormat;
import java.net.InetAddress;

public class Client {

    private String STARTUP_DIR;
    private String EXEC_SCRIPT;
    private int REFRESH_TIME;
    private int WARNING_TIME;
    private int CORRECTION_TIME;
    private String HOST_IP;
    private DBAccess dba;

    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("Terminal identifier: "+args[0]);
            new Client(args[0]);
        } else {
            System.out.println("There is not enough input parameters!");
            System.exit(1);
        }
    }

    public Client(String comp) {
        try {
            readConfig("de_shell.conf");

            boolean warn = true;
            String message;
            Random randomGenerator = new Random();
       	    Date beginDate = new Date();
            Date endDate = new Date();
            String sessionType;

            dba.connect();
            dba.getSessionInfo(comp,CORRECTION_TIME);
            sessionType = dba.getSessionType(comp);
            shellExec("profile "+sessionType);
            if (sessionType != null) {
                System.out.println("Extended session type: "+sessionType);
                System.exit(0);
            } else if (dba.END_DATE == null || dba.BEGIN_DATE == null || dba.REID < 1) {
                System.out.println("This session isn't found in database!");
                System.exit(1);
            }
            dba.setSessionRow(HOST_IP);

            beginDate = strToDate(dba.BEGIN_DATE);
            endDate = strToDate(dba.END_DATE);
            long remainedTime = (endDate.getTime()-System.currentTimeMillis())/1000;

            TimerGUI tg = new TimerGUI(beginDate, endDate, comp);
            tg.run();

            while(remainedTime > REFRESH_TIME) {
                //System.out.println("loop");
                Thread.sleep(REFRESH_TIME*1000+randomGenerator.nextInt(REFRESH_TIME*100));

                dba.getSessionTime(CORRECTION_TIME);
                beginDate = strToDate(dba.BEGIN_DATE);
                endDate = strToDate(dba.END_DATE);
                tg.setTime(beginDate, endDate);

                remainedTime = (endDate.getTime()-System.currentTimeMillis())/1000;

                if (warn && remainedTime <= WARNING_TIME && remainedTime > REFRESH_TIME) {
                    System.out.println("Show warning message");
                    shellExec("warn info");
                    warn = false;
                }

                message = dba.getMessage();
                if (message != null) {
                   System.out.println("Show message: "+message);
                   shellExec("message "+message);
                }

            }
            dba.setEndTimestamp();
            dba.disconnect();
            shellExec("warn exit");
            System.out.println("Session time has expired");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Date strToDate(String str) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = sdf.parse(str);
        return date;
    }

    public void readConfig(String conf) throws Exception {
        Properties prop = System.getProperties();
        STARTUP_DIR = prop.getProperty("user.dir")+prop.getProperty("file.separator");
        HOST_IP = InetAddress.getLocalHost().getHostAddress();

        prop = new Properties();
        FileInputStream fin1 = new FileInputStream(STARTUP_DIR+conf);
        prop.load(fin1);
        fin1.close();

        REFRESH_TIME = Integer.valueOf(prop.getProperty("refresh_time")).intValue();
        WARNING_TIME = Integer.valueOf(prop.getProperty("warning_time")).intValue();
        CORRECTION_TIME = Integer.valueOf(prop.getProperty("correction_time")).intValue();
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

}
