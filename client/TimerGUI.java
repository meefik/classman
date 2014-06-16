import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
//import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import java.text.SimpleDateFormat;
import java.util.*;


public class TimerGUI extends JFrame implements Runnable {

    private Timer timer;
    private Date endDate;
    private Date beginDate;
    private String compID;
    private JLabel timerLabel;
    private JLabel beginTimeLabel;
    private JLabel endTimeLabel;
    private JFrame frame;

    public TimerGUI( Date beginDate, Date endDate, String compID ) {
        this.beginDate = beginDate;
	this.endDate = endDate;
        this.compID = compID;
    }

    public void setTime( Date beginDate, Date endDate ) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    private final Timer getTimer() {
        if (timer == null) {
            int delay = 1000; // milliseconds
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    taskPerformed();
                }
            };
            timer = new Timer(delay, taskPerformer);
            timer.setInitialDelay(0);
        }
        return timer;
    }

    private final void taskPerformed() {
        long time = endDate.getTime()-System.currentTimeMillis(); // milliseconds
        if (time < 1) {
	    stopTimer();
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String str = sdf.format(new Date(time));
        timerLabel.setText(str);
        frame.setTitle("["+compID+"] Таймер: "+str);
        sdf.setTimeZone(TimeZone.getDefault());
        str = sdf.format(beginDate);
        beginTimeLabel.setText(str);
        str = sdf.format(endDate);
        endTimeLabel.setText(str);
    }
/*
    public static void main(String[] args) {
        SwingUtilities.invokeLater (new TimerGUI());
    }
*/
    public void run() {
        frame = new JFrame ("Оставшееся время");
	frame.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
	frame.setResizable(false);
        Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());

        timerLabel = new JLabel("00:00:00");
        timerLabel.setBorder(new TitledBorder(new EtchedBorder(), "Оставшееся время"));
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 80));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(1,2));

        beginTimeLabel = new JLabel("00:00:00");
        beginTimeLabel.setBorder(new TitledBorder(new EtchedBorder(), "Время начала"));
        beginTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 38));

        endTimeLabel = new JLabel("00:00:00");
        endTimeLabel.setBorder(new TitledBorder(new EtchedBorder(), "Время окончания"));
        endTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 38));

        panel1.add(beginTimeLabel);
        panel1.add(endTimeLabel);

        c.add(timerLabel, BorderLayout.NORTH);
        c.add(panel1, BorderLayout.SOUTH);

        frame.pack();
	startTimer();
    }

    public final synchronized void startTimer() {
        //endTime = System.currentTimeMillis();
        frame.setVisible(true);
        getTimer().start();
    }

    public final synchronized void stopTimer() {
        getTimer().stop();
        frame.setVisible(false);
	frame.dispose();
    }
}
