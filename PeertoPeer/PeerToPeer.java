import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

/**
 * @authors Morgan, Fahim, Zakaria
 * 
 * Implements a peer to peer network where peers can send and receive messages
 */

public class PeerToPeer {

    // packet/socket sending details
    private DatagramSocket socket;
    private int packetNumber = 1;

    private static ExecutorService executorService;

    private static final ThreadLocal<Integer> threadNumber = new ThreadLocal<>();

    // node checking details
    HashMap<InetAddress, Integer> map = new HashMap<>();
    HashMap<InetAddress, String> mapAvailable = new HashMap<>();
    ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);

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
     * Constructor for PeerToPeer class. Initializes the socket and thread pool.
     */
    public PeerToPeer() {
        try {
            // create the socket reading from the config and MySocket text file
            File inFile = new File("Config.txt");
            File mySocket = new File("MySocket.txt");
            Scanner scan = new Scanner(inFile);
            Scanner myPort = new Scanner(mySocket);

            int port = Integer.parseInt(myPort.nextLine().trim());

            // Bind socket to the specified local port
            socket = new DatagramSocket(port);
            System.out.println("Bound to local port: " + socket.getLocalPort());

            if (scan.hasNextLine()) {
                String localIP = scan.nextLine().trim();
            } else {
                System.out.println("Config file is empty or incorrect format.");
                socket = new DatagramSocket(); // Default to system-assigned port
            }

            scan.close();

            // create a thread pool with 4 threads
            executorService = Executors.newFixedThreadPool(4);
        } catch (FileNotFoundException e) {
            System.out.println("Config file not found. Using default port.");
            try {
                socket = new DatagramSocket();
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends messages to other peers.
     * 
     * @param localIPAddress The local IP address of this peer.
     */
    @SuppressWarnings("deprecation")
    public void sendMessages(InetAddress localIPAddress) {
        threadNumber.set((int) (Thread.currentThread().getId() % 4) + 1);

        try {
            while (true) {
                String[] fileList = getFileListing();

                //reads in  config
                try {
                    File inFile = new File("Config.txt");
                    Scanner scan = new Scanner(inFile);
                    System.out.println("Sending message #" + packetNumber);

                    //checks if this port is this system's
                    while (scan.hasNextLine()) {
                        String ip = scan.nextLine().trim();
                        if (!scan.hasNextLine()) {
                            System.out.println("Config file format incorrect.");
                            break;
                        }
                        String port = scan.nextLine().trim();
                        InetAddress remoteIPAddress = InetAddress.getByName(ip);
                        System.out.println(ip + " " + port);
                        int remotePort = Integer.parseInt(port);

                        OurProtocol packet = new OurProtocol(remoteIPAddress, localIPAddress, remotePort,
                                socket.getLocalPort(), packetNumber, fileList);

                        socket.send(packet.getPacket());
                    }
                    scan.close();
                    heartBeat();
                    packetNumber++;
                } catch (FileNotFoundException e) {
                    System.out.println("The file you inputted does not exist."); // if file input is invalid
                } catch (Exception e) {
                    System.out.println("An error has occurred"); // if alternate error has occurred
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives messages from other peers.
     */
    public void receiveMessages() {
        byte[] incomingData = new byte[1024];
        DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

        while (true) {
            try {
                socket.receive(incomingPacket);

                // put hash
                synchronized (map) {
                    map.put(incomingPacket.getAddress(), 0);
                    mapAvailable.put(incomingPacket.getAddress(), " - Alive");
                }

                OurProtocol deconstructPacket = new OurProtocol(incomingPacket);
                deconstructPacket.protocolDetails();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks the heartbeat of other peers.
     */
    public void checkHeartbeat() {
        timer.scheduleAtFixedRate(() -> {
            synchronized (map) {
                for (Map.Entry<InetAddress, Integer> entry : map.entrySet()) {
                    if (entry.getValue() >= 30) {
                        mapAvailable.put(entry.getKey(), " - Dead");
                    }
                }
                System.out.println("Current connections: " + mapAvailable);
            }
        }, 0, 30, TimeUnit.SECONDS);

        timer.scheduleAtFixedRate(() -> {
            synchronized (map) {
                for (Map.Entry<InetAddress, Integer> entry : map.entrySet()) {
                    map.put(entry.getKey(), entry.getValue() + 1);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Starts all threads for the peer-to-peer network.
     * 
     * @param localIPAddress The local IP address of this peer.
     */
    public void start(InetAddress localIPAddress) {
        System.out.println("Starting all threads...");

        executorService.submit(() -> {
            System.out.println("Thread 1: Receiving messages started.");
            receiveMessages();
        });

        executorService.submit(() -> {
            System.out.println("Thread 2: Sending messages started.");
            sendMessages(localIPAddress);
        });

        executorService.submit(() -> {
            System.out.println("Thread 3: Checking heartbeat started.");
            checkHeartbeat();
        });

        executorService.submit(() -> {
            System.out.println("Thread 4: Logger started.");
        });
    }

    /**
     * Retrieves the local IP address of this peer.
     * 
     * @return The local IP address, or null if it cannot be determined.
     */
    public static InetAddress getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
            return InetAddress.getLocalHost();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public static void main(String[] args) {
        InetAddress localIPAddress = getLocalIPAddress();
        if (localIPAddress != null) {
            System.out.println("Local IP Address: " + localIPAddress.getHostAddress());
            System.out.println(localIPAddress);

            PeerToPeer client = new PeerToPeer();
            client.start(localIPAddress);
        } else {
            System.out.println("Failed to obtain local IP address.");
        }
    }
}