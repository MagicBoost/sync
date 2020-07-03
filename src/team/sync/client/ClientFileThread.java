package team.sync.client;

public class ClientFileThread extends Thread{
    int port;
    String path;
    int sleepTime;

    public ClientFileThread(int port, String path, int sleepTime) {
        this.port = port;
        this.path = path;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TCPClientFile fileClient = new TCPClientFile(port, path);
                fileClient.createSocket();
                fileClient.receiveDirStruct();
                fileClient.receiveFile();
                sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
