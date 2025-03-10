import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

/**
 * @author Morgan Fahim Zakaria 
 * 
 * Implements a UDP client that sends messages to a UDP server
 */
public class UDPClient {

    // Datagram socket for communication
    private DatagramSocket Socket;
    private int packetNumber = 0;

    /**
     * Retrieves the list of files in the current directory.
     * 
     * @return An array of file names in the current directory.
     */
    private static String[] getFileListing() {
        String filePath = System.getProperty("user.dir");
        File homeDirectory = new File(filePath);
        File[] fileListing = homeDirectory.listFiles();

        if (fileListing == null) {
            return new String[] { "EMPTY" };
        }

        List<String> fileNames = new ArrayList<>();
        for (File file : fileListing) {
            if (!file.getName().equals(".git")) {
                fileNames.add(file.getName());
            }
        }

        return fileNames.toArray(new String[0]);
    }

    /**
     * Simulates a heartbeat by sleeping for a random number of seconds.
     */
    public static void heartBeat() {
        SecureRandom random = new SecureRandom();
        int sec = random.nextInt(30) + 1;
        System.out.println("Waiting for " + sec + " seconds");

        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for UDPClient class.
     */
    public UDPClient() {
       
    }

    /**
     * Creates a socket and sends messages to the server, then waits for responses.
     */
    public void createAndListenSocket() {
        try {
            while (true) {
                // Read the server IP address from the ServerSocket.txt file
                File inFile = new File("ServerSocket.txt");
                String line = "";
                Scanner scan = new Scanner(inFile);
                line = scan.nextLine();
                int serverPort = Integer.parseInt(scan.nextLine());
                scan.close();

                // Create a datagram socket
                Socket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(line);
                InetAddress localIPAddress = InetAddress.getLocalHost(); // Get the local IP address
                byte[] incomingData = new byte[1024];

                // Set timeout to 5 seconds
                Socket.setSoTimeout(5000);

                // Create and send a packet
                String[] fileList = getFileListing();
                OurProtocol newPacket = new OurProtocol(IPAddress, localIPAddress, serverPort,
                        Socket.getLocalPort(), packetNumber++, fileList);

                heartBeat();
                Socket.send(newPacket.getPacket());
                System.out.println("Message sent from client");

                try {
                    // Receive response from server
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    Socket.receive(incomingPacket);
                    String response = new String(incomingPacket.getData());
                    
                    //print out nodes
                    String[] dataParts = response.split("\\$");
                    for(String ipInfo : dataParts){
                        System.out.println(ipInfo);
                    }
                    
                } catch (SocketTimeoutException e) {
                    System.out.println("No response from server, resending...");
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        client.createAndListenSocket();
    }
}