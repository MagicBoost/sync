package team.sync.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SyncFrameClient extends JFrame {
    //sync parameter
    private static String runningIp, tempIp = null;
    private static int runningPort, tempPort = 0;
    private static String runningPath, tempPath = null;
    private static String runningOption, tempOption = null;
    private static Boolean syncBool = true;

    //Component & Container
    JPanel infoPanel = new JPanel();
    JLabel infoLabel = new JLabel("Hello World!");

    JPanel mainPanel = new JPanel();
    JLabel iPLabel = new JLabel("数据资源所属IP", SwingConstants.RIGHT);
    JLabel portLabel = new JLabel("端口号", SwingConstants.RIGHT);
    JLabel pathLabel = new JLabel("路径", SwingConstants.RIGHT);
    JLabel optionLabel = new JLabel("同步选项", SwingConstants.RIGHT);
    JTextField iPText = new JTextField();
    JTextField portText = new JTextField();
    JTextField pathText = new JTextField();
    JComboBox<String> optionComboBox = new JComboBox<String>();

    JPanel buttonPanel = new JPanel();
    JButton syncButton = new JButton("sync");

    public SyncFrameClient() {
        //set component
        iPText.setText("127.0.0.1");
        portText.setText("5000");
        optionComboBox.addItem("实时同步");
        optionComboBox.addItem("每30秒同步");
        optionComboBox.addItem("每小时同步");
        optionComboBox.addItem("每天同步");

        //Layout
        this.setLayout(new BorderLayout(5, 5));
        infoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        mainPanel.setLayout(new GridLayout(4, 2, 5, 5));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        //add Component
        infoPanel.add(infoLabel);
        mainPanel.add(iPLabel);
        mainPanel.add(iPText);
        mainPanel.add(portLabel);
        mainPanel.add(portText);
        mainPanel.add(pathLabel);
        mainPanel.add(pathText);
        mainPanel.add(optionLabel);
        mainPanel.add(optionComboBox);
        buttonPanel.add(syncButton);

        this.add(infoPanel, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        //button event over thread
        syncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new ButtonListener()).start();
            }
        });

        //set Frame
        this.setTitle("文件同步");
        this.setSize(300, 250);
        this.setResizable(false);
        this.getContentPane().setBackground(Color.white);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    //specific sync button thread
    class ButtonListener implements Runnable {
        @Override
        public void run() {
            try {
                //get current options
                String ip = iPText.getText();
                int port = Integer.parseInt(portText.getText());
                String path = pathText.getText();
                String option = optionComboBox.getSelectedItem().toString();

                //current options must be different from running and temp options
                if (!ip.equals(runningIp) || port != runningPort || !path.equals(runningPath) || !option.equals(runningOption)) {
                    if (!ip.equals(tempIp) || port != tempPort || !path.equals(tempPath) || !option.equals(tempOption)) {
                        tempIp = ip;
                        tempPort = port;
                        tempPath = path;
                        tempOption = option;

                        //check lock
                        while (syncBool != true) {
                            Thread.sleep(1000);
                        }
                        syncBool = false;

                        //insure current thread's sync options will not be covered
                        if (ip.equals(tempIp) && port == tempPort && path.equals(tempPath) && option.equals(tempOption)) {
                            runningIp = tempIp;
                            runningPort = tempPort;
                            runningPath = tempPath;
                            runningOption = tempOption;
                            tempIp = null;
                            tempPort = 0;
                            tempPath = null;
                            tempOption = null;

                            //check other threads require
                            while (tempIp == null && tempPort == 0 && tempPath == null && tempOption == null) {
                                infoLabel.setText("同步中");
                                //TODO 此处添加同步操作

                                TCPClientFile fileClient = new TCPClientFile(port, path);
                                fileClient.clientStart();

                                infoLabel.setText("同步暂停");
                                if (option.equals("实时同步")) Thread.sleep(10000);
                                else if (option.equals("每30秒同步")) Thread.sleep(30000);
                                else if (option.equals("每小时同步")) Thread.sleep(3600000);
                                else Thread.sleep(86400000);
                            }
                            syncBool = true;

                        }

                    }
                }
            } catch (NumberFormatException | InterruptedException | NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        SyncFrameClient sfc = new SyncFrameClient();
        sfc.setVisible(true);

    }
}
