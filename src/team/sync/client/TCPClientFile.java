package team.sync.client;

import java.io.*;
import java.net.Socket;

public class TCPClientFile {

    private Socket socket = null;
    private DataInputStream inStream = null;
    private DataOutputStream outStream = null;
    private int port;
    private String backupDir;

    /**
     * 构造函数
     * @param port - 端口号
     * @param backupDir - 目标目录
     */
    public TCPClientFile(int port, String backupDir) {
        this.port = port;
        this.backupDir = backupDir;
        File file = new File(backupDir);
        file.mkdir();
    }

    /**
     * 创建套接字的方法
     * 获取输入和输出流
     */
    public void createSocket() {
        try {
            // 连接
            socket = new Socket("127.0.0.1", port);
            System.out.println("Connected");
            // 获取输入/输出流
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 接收目录结构的方法
     * 它在backupdir中创建接收的目录结构
     */
    public void receiveDirStruct() {
        try {
            int dirNo = inStream.read(); // 接收文件数
      
            for (int count = 0; count < dirNo; count++) {
                int flag = inStream.read();
                if (flag == 1) { // 是文件夹
                    flag = 0;
                    String absPath = inStream.readUTF(); // 文件夹路径
                    System.out.println("Relative path of dir to be created: " + absPath);
                    File newDir = new File(backupDir, absPath);
                    newDir.mkdir();
                    System.out.println("Directory created: " + newDir.getAbsolutePath() + "\n");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
   
    public void receiveFile() {
	    reInitConn();
	    // final int MAX_BUFFER = 1000;
	    byte[] data = null;
        int fileNum = 0;

        try {
            fileNum = inStream.readInt(); // 接收文件数量
            System.out.println("Receiving No of Files:" + fileNum + "\n");
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        
     
        for (int i = 0; i < fileNum; i++) {
            try {
                String received = inStream.readUTF(); // 文件相对路径
          
                File newFile = new File(backupDir, received); // 创建文件对象
                long fileSize = inStream.readLong(); // 文件长度
                int bufferSize;

                // 决定输入缓冲区大小
//                if(fileSize > MAX_BUFFER)
//                    bufferSize = MAX_BUFFER;
//                else
//                    bufferSize = (int)fileSize;
                bufferSize = (int)fileSize;
                data = new byte[bufferSize];

                FileOutputStream fileOut = new FileOutputStream(newFile.getAbsolutePath());

                // 读取文件
                long totalBytesRead = 0;
                
                while(true) {
                    int readBytes = inStream.read(data,0, bufferSize); // 读文件
                    byte[] arrayBytes = new byte[readBytes];
                    System.arraycopy(data, 0, arrayBytes, 0, readBytes);
                    totalBytesRead += readBytes;
                    
                    if(readBytes > 0) {
                        // 写入文件
                        fileOut.write(arrayBytes);
                        fileOut.flush();
                    }

                    // 如果收到fileSize就停止
                    if(totalBytesRead == fileSize)
                        break;

                    // 更新文件大小以获取最后剩余的数据块
                    //if((fileSize - totalBytesRead) < MAX_BUFFER)
                    bufferSize = (int) (fileSize - totalBytesRead);
                
                    // 重置缓冲区
                    data = new byte[bufferSize];
                }
                System.out.println(newFile.getAbsolutePath() + " is received");
                fileOut.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        // 关闭流
        try {
            inStream.close();
            outStream.close();
            socket.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 重置连接
     */
    private void reInitConn() {
	    try {
		    inStream.close();
		    outStream.close();
		    socket.close();
		    //socket = new Socket("192.168.1.93" , port);
            socket = new Socket("127.0.0.1" , port);
		    outStream = new DataOutputStream(socket.getOutputStream());
		    inStream = new DataInputStream(socket.getInputStream());
	    }
	    catch (IOException e) {
		    e.printStackTrace();
	    }
    }

    public static void main(String[] args) throws Exception {
        TCPClientFile fileClient = new TCPClientFile(6002, "C:\\distr_exp\\FileSync\\res");
        fileClient.createSocket();
        fileClient.receiveDirStruct();
        fileClient.receiveFile();
    }
}