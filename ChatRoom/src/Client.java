import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Keven Lin
 */
public class Client {

    private static final String host = "localhost";
    private static final int portNumber = 1234;

    private String userName;
    private Scanner userInputScanner;
    private String ChatServer;
    private int serverPort;

    /**
     * Client Constructor
     * @param userName
     * @param host
     * @param portNumber
     */
    private Client(String userName, String host, int portNumber){
        this.userName = userName;
        this.ChatServer = host;
        this.serverPort = portNumber;
    }

    /**
     * 1. wait 2 seconds for network connection (allow for sometime for network delay)
     * 2. connect to the client server
     * 3. if anything breaks the client will spit out some error code for debugging
     * @param scan
     */
    private void ClientRunner(Scanner scan){
        try{
            Socket socket = new Socket(ChatServer, serverPort);
            Thread.sleep(2000);

            ServerThread serverThread = new ServerThread(socket, userName);
            Thread serverAccessThread = new Thread(serverThread);
            serverAccessThread.start();
            while(serverAccessThread.isAlive()){
                if(scan.hasNextLine()){
                    serverThread.addNextMessage(scan.nextLine());
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * Run the Client side
     * 1. the username must not have any whitespace and it cannot be empty/null
     * 2. then take that information and sent it to the Client runner
     * @param args
     */
    public static void main(String[] args){
        String InputUserName = null;
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter your name: (After giving input \"name\", then press Enter)");
        while(InputUserName == null || InputUserName.trim().equals("")){
            InputUserName = scan.nextLine();
            if(InputUserName.trim().equals("")){
                System.out.println("Invalid. Please enter again:");
            }
        }
        Client client = new Client(InputUserName, host, portNumber);
        client.ClientRunner(scan);
    }
}

/**
 * @author Keven Lin
 * create a new client for every new client
 */
class ClientThread implements Runnable {
    private PrintWriter clientOut;
    private ChatRoomServer server;
    private Socket socket;

    private PrintWriter getWriter(){
        return clientOut;
    }

    public ClientThread(ChatRoomServer server, Socket socket){
        this.server = server;
        this.socket = socket;
    }

    /**
     * as long as the client is connected to the server, the client will continuously sent messages to the server
     */
    @Override
    public void run() {
        try{
            this.clientOut = new PrintWriter(socket.getOutputStream(), false);
            Scanner in = new Scanner(socket.getInputStream());

            while(!socket.isClosed()){
                if(in.hasNextLine()){
                    String input = in.nextLine();
                    System.out.println(input);
                    for(ClientThread thatClient : server.getClients()){
                        PrintWriter thatClientOut = thatClient.getWriter();
                        if(thatClientOut != null){
                            thatClientOut.write(input + "\r\n");
                            thatClientOut.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}