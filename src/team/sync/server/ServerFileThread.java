package team.sync.server;

public class ServerFileThread extends Thread {
    int port;
    String path;

    public ServerFileThread(int port, String path) {
        this.port = port;
        this.path = path;
    }

    @Override
    public void run() {
        while (true) {
            TCPServerFile fileServer = new TCPServerFile(port, path);
            fileServer.createSocket();
            fileServer.sendDirStruct();
            fileServer.sendFile();
        }
    }
}
