package team.sync.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SyncFrameClient extends JFrame{
    //sync parameter
    private static String ip;
    private static int port;
    private static String path;
    private static String option;

    //Component & Container
    JPanel infoPanel = new JPanel();
    JLabel infoLabel = new JLabel("Hello World!");

    JPanel mainPanel = new JPanel();
    JLabel iPLabel = new JLabel("数据资源所属IP",SwingConstants.RIGHT);
    JLabel portLabel = new JLabel("端口号",SwingConstants.RIGHT);
    JLabel pathLabel = new JLabel("路径",SwingConstants.RIGHT);
    JLabel optionLabel =new JLabel("同步选项",SwingConstants.RIGHT);
    JTextField iPText = new JTextField();
    JTextField portText = new JTextField();
    JTextField pathText = new JTextField();
    JComboBox<String> optionComboBox = new JComboBox<String>();

    JPanel buttonPanel = new JPanel();
    JButton syncButton = new JButton("sync");

    public SyncFrameClient(){
        //set component
        iPText.setText("127.0.0.1");
        portText.setText("5000");
        optionComboBox.addItem("实时同步");
        optionComboBox.addItem("每小时同步");
        optionComboBox.addItem("每天同步");

        //Layout
        this.setLayout(new BorderLayout(5,5));
        infoPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        mainPanel.setLayout(new GridLayout (4,2,5,5));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));

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
        this.add(mainPanel,BorderLayout.CENTER);
        this.add(buttonPanel,BorderLayout.SOUTH);

        //events
        syncButton.addActionListener(new team.sync.client.SyncFrameClient.ButtonListener());

        //set Frame
        this.setTitle("文件同步");
        this.setSize(300,250);
        this.setResizable(false);
        this.getContentPane().setBackground(Color.white);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    //Sync Button event
    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            ip = iPText.getText();
            port = Integer.parseInt(portText.getText());
            path= pathText.getText();
            option = optionComboBox.getSelectedItem().toString();
            //infoLabel.setText(option);
            System.out.println(option);
            // TODO 应根据option增加关于同步时间的操作
            // TODO 是否需要使用多线程?不适用多线程会导致swing绘制线程堵塞，无法更改Label文本

            int sleepTime;
            if (option.equals("实时同步")) sleepTime = 10000;
            else if (option.equals("每小时同步")) sleepTime = 3600000;
            else sleepTime = 86400000;

            ClientFileThread clientThread = new ClientFileThread(port, path, sleepTime);
            clientThread.start();
        }
    }


    public static void main(String[] args) {
        new SyncFrameClient();
    }
}
