package team.sync.server;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerFile {

   	private ServerSocket serverSocket = null;
   	private Socket socket = null;
   	private DataInputStream inStream = null;
   	private DataOutputStream outStream = null;
   	private ArrayList<File> toTransfer = new ArrayList<File>(); // 需要传送的文件数组
   	private String dirName; // 需同步的文件夹路径
   	private int port;
   	private int fileCount; // 文件数
	private int dirCount; // 文件夹数

	/**
	 * 构造函数
 	 * @param port - 端口
 	 * @param dir - 源目录
 	 */
	public TCPServerFile(int port, String dir) {
    	this.port = port;
    	this.dirName = dir;
    	this.getFile(dirName);
    	System.out.println("Contents count: (file)" + fileCount + "(dir)" + dirCount);
  	}

	/**
 	 * 创建套接字，获取输入输出流
 	 */
	public void createSocket() {
		try {
			// 创建服务，启动监听
			serverSocket = new ServerSocket(port);
			// 接收连接
			socket = serverSocket.accept();
			// 获取流
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
			System.out.println("Connected");
			System.out.println("Dir to sync: " + dirName);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }

	/**
 	 * 将目录发送到客户端
 	 * 辅助方法遍历目录
 	 */
	public void sendDirStruct() {
    	File src = new File(dirName);
    	String child[] = src.list(); // 获取dirName目录下的文件
    	try {
    		outStream.write(dirCount); // 发送需同步的文件数
    		outStream.flush();
    	}
    	catch(IOException h) {
    		h.printStackTrace();
    	}
    
    	
    	for(int i = 0 ; i < child.length ; i++) {
    		iterate(new File(src, child[i]));
      	}
  	}

	/**
	 * 用于遍历目录的方法
  	 * 检查源目录中的内容是文件还是目录
  	 * 如果是文件存储在ArrayList中
  	 * 如果是目录则将其相对路径发送给客户端
  	 * @param dir
  	 */
	private void iterate(File dir) {
    	try {
    		if (dir.isDirectory()) {
    			outStream.write(1); // 是文件夹
    			outStream.flush();
    			String srcRelative = dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(dirName) + dirName.length() + 1));
    			System.out.println("Relative path of directory to be sent: " + srcRelative);
    			outStream.writeUTF(srcRelative);
				outStream.flush();
				String child[] = dir.list();
				for(int i = 0 ; i < child.length ; i++) {
					iterate(new File(dir , child[i]));
			 	}
		 	}
    	 	else {
    	    	toTransfer.add(dir);
    	  	}
       	}
      	catch (IOException e) {
    		e.printStackTrace();
	   	}
	}
	/**
 	 * 计算文件数
 	 * @param dirName - 需要计算文件数的文件夹
 	 */
	private void getFile(String dirName) {
    	File f = new File(dirName);
    	File[] files = f.listFiles();
    
    	if (files != null)
    	for (int i = 0; i < files.length; i++) {
        	File file = files[i];
        	if (file.isDirectory()) {
        		dirCount++;
             	getFile(file.getAbsolutePath());
        	}
        	else {
        		fileCount++;
			}
    	}
	}
    	
 	/**
  	 * 发送文件和相对路径
  	 */
 	public void sendFile() {
		reInitConn();
		// final int MAX_BUFFER = 1000;
    	byte [] data = null;
    	int bufferSize = 0;
    	
    	try {
       		outStream = new DataOutputStream(socket.getOutputStream());
       
       		System.out.println("\nSending No of files: " + toTransfer.size() + "\n");
       		int size = toTransfer.size();
       		outStream.writeInt(size); // 发送传送文件数量
	   		outStream.flush();
	 	}
    	catch (IOException e1) {
			e1.printStackTrace();
	  	}
    	
     	for (int i = 0; i < toTransfer.size(); i++) {
    	 	try {
    		 	// 文件相对路径
    			String s = (toTransfer.get(i).getAbsolutePath().substring((toTransfer.get(i).getAbsolutePath().indexOf(dirName) + dirName.length()+1)));
    			System.out.println("Relative paths to be sent: " + s);
    		
    			outStream.writeUTF(s);
    			outStream.flush();
    		
    			FileInputStream fileInput = new FileInputStream(toTransfer.get(i));
    		
    			// 文件长度
    			long fileSize = toTransfer.get(i).length();
    			System.out.println("File size at server is: " + fileSize + " bytes");
    		
    			// 发送文件长度
    			outStream.writeLong(fileSize);
    			outStream.flush();

    			// 发送文件内容
//    			if(fileSize > MAX_BUFFER)
//    		  		bufferSize = MAX_BUFFER;
//    			else
//    		  		bufferSize = (int)fileSize;
				bufferSize = (int)fileSize;

    			data = new byte[bufferSize];
    		
    			long totalBytesRead = 0;
    			while(true) {
    				// 文件读到data中
    				int readBytes = fileInput.read(data);
    				// 发送文件
    				outStream.write(data);
    				outStream.flush();
    				// EOF
    				if(readBytes == -1)//EOF
    					break;
    			
    				totalBytesRead += readBytes;
    				// 如果读到fileSize就停止
    				if(totalBytesRead == fileSize)
    					break;
    				// 更新文件大小以获取最后剩余的数据块
    				//if((fileSize - totalBytesRead) < MAX_BUFFER)
					bufferSize = (int) (fileSize - totalBytesRead);
    			
    				// 重置缓冲区
    				data = new byte[bufferSize];
    		  	}
    		  	System.out.println(toTransfer.get(i) + " is Sent " + "\n");
    		  	fileInput.close();
    		    		
    	   	}
    	   	catch(Exception e) {
    		 	e.printStackTrace();
    	 	}
    	}

    	try {
		   	inStream.close();
		   	outStream.close();
		   	serverSocket.close();
		   	socket.close();
    	}
		catch (Exception e) {
			e.getStackTrace();
		}
    }

 	/**
  	 * 重启连接，获取新的socket流
  	 */
 	private void reInitConn() {
		try {
			inStream.close();
			outStream.close();
			socket.close();
			socket = serverSocket.accept();
			outStream = new DataOutputStream(socket.getOutputStream());
			inStream = new DataInputStream(socket.getInputStream());
	 	}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
    	TCPServerFile fileServer = new TCPServerFile(6002 , "C:\\distr_exp\\FileSync\\source");
    	fileServer.createSocket();
    	fileServer.sendDirStruct();
    	fileServer.sendFile();
    }
}