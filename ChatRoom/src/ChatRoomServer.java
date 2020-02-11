import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Keven Lin
 */
public class ChatRoomServer {

    private static final int portNumber = 1234;
    private int ClientPort;
    private List<ClientThread> clients;

    /**
     * Constructor the clients port number
     * @param portNumber
     */
    public ChatRoomServer(int portNumber){
        this.ClientPort = portNumber;
    }

    /**
     * get clients that are online
     * @return
     */
    public List<ClientThread> getClients(){
        return clients;
    }

    /**
     * Starts the server and perform checks to make sure that server can listen to the port
     */
    private void startServer(){
        clients = new ArrayList<ClientThread>();
        ServerSocket ClientSocket = null;
        try {
            ClientSocket = new ServerSocket(ClientPort);
            ClientSocketConnection(ClientSocket);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * if the client can connect to the chat room server, that is great!
     * If not, the server would spit out error that it failed to accept the port number that the client used
     * @param serverSocket
     */
    private void ClientSocketConnection(ServerSocket serverSocket){
        while(true){
            try{
                Socket socket = serverSocket.accept();
                ClientThread client = new ClientThread(this, socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } 
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * main tread for the server
     * @param args
     */
    public static void main(String[] args){
        ChatRoomServer server = new ChatRoomServer(portNumber);
        server.startServer();
    }
}

/**
 * @author Keven Lin
 * helper class that will create a new thread for every new connection from the client
 */
class ServerThread implements Runnable {
    private Socket socket;
    private String UserName;
    private final LinkedList<String> ChatMessage;
    private boolean hasMessages = false;

    /**
     * constructor the the threads
     * @param socket
     * @param UserName
     */
    public ServerThread(Socket socket, String UserName){
        this.socket = socket;
        this.UserName = UserName;
           ChatMessage = new LinkedList<String>();
    }

    /**
     * this class synchronized the messages to prevent any file corruption
     * then delivers it to the client
     * @param message
     */
    public void addNextMessage(String message){
        synchronized (ChatMessage){
            hasMessages = true;
            ChatMessage.push(message);
        }
    }

    /**
     * password checker
     */
    public void passwordChecker() {
        Scanner scanner = new Scanner(System.in);
        String pass = null;
        System.out.print("Enter access code: (PRESS ENTER FIRST then type in \"access code\" then press enter)");
        pass = scanner.nextLine();
        if (!pass.equals("cs319spring2020")) {
            System.out.println("Incorrect Access Code");
            passwordChecker();
        }
    }


    /**
     * 1. checks the access code of the client and make sure it is correct (will continuslly repeat until the
     *      the password is correct
     * 2. if the password is correct then the user is greeted with a connection
     * 3. message that the client type will not only print on the server side, but is broadcast to all the clients
     */
    @Override
    public void run(){
        passwordChecker();
        System.out.println("you are connected");
        System.out.println("Welcome :" + UserName);

        try{
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), false);
            InputStream fromClient = socket.getInputStream();
            Scanner serverIn = new Scanner(fromClient);

            while(!socket.isClosed()){
                if(fromClient.available() > 0){
                    if(serverIn.hasNextLine()){
                        System.out.println(serverIn.nextLine());
                    }
                }
                if(hasMessages){
                    String nextMessage = "";
                    synchronized(ChatMessage){
                        nextMessage = ChatMessage.pop();
                        hasMessages = !ChatMessage.isEmpty();
                    }
                    toClient.println(UserName + " --> " + nextMessage);
                    toClient.flush();
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
}
