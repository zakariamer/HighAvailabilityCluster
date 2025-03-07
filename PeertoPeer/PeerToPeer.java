//package PeertoPeer;

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

public class PeerToPeer {

    // hint:Use a concurent hash map(static reference)to create the nodes , store socket and data of other computers

    // hint:Create 3 three threads those 3 are recieve, send/heartbeat and check heartbeat(Determine if the nodes )
    // hint: the fourth thread is main for printing 

    //packet/socket sending details
    private DatagramSocket socket;
    private int packetNumber = 1;

    private static  ExecutorService executorService;
   
    private static final ThreadLocal<Integer> threadNumber = new ThreadLocal<>();
    
    //node checking details
    HashMap<InetAddress,Integer> map = new HashMap<>();
    HashMap<InetAddress,String> mapAvailable = new HashMap<>();
    ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);

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

    public PeerToPeer() {
       
        
        try {
            // create the socket assuming the server is listening on port 9876
            File inFile = new File("PeerToPeer/Config.txt");
            Scanner scan = new Scanner(inFile);

            if (scan.hasNextLine()) {
                String localIP = scan.nextLine().trim();
    
                // Bind socket to the specified local port
                socket = new DatagramSocket(9877);
                System.out.println("Bound to local port: " + socket.getLocalPort());
            } else {
                System.out.println("Config file is empty or incorrect format.");
                socket = new DatagramSocket(); // Default to system-assigned port
            }

            scan.close();

            // create a thread pool with 5 threads
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

    @SuppressWarnings("deprecation")
    public void sendMessages(InetAddress localIPAddress) {

       
        threadNumber.set((int) (Thread.currentThread().getId()% 4) + 1);
       

        try {
            while (true) {
                String[] fileList = getFileListing();
               
                
                
                // read config
                try{
                    File inFile = new File("PeerToPeer/Config.txt");
                    Scanner scan = new Scanner(inFile);


                    System.out.println("Sending message #" + packetNumber);

                    //checks if this port is this system's 
                    while (scan.hasNextLine()) {
                        String ip = scan.nextLine().trim();
                        if(!scan.hasNextLine()){
                            System.out.println("Config file format incorrect.");
                            break;
                        }
                        String port = scan.nextLine().trim();
                        InetAddress remoteIPAddress = InetAddress.getByName(ip);
                        System.out.println(ip + " " + port);
                        int remotePort = Integer.parseInt(port);

                        OurProtocol packet = new OurProtocol(remoteIPAddress, localIPAddress, remotePort, socket.getLocalPort(), packetNumber, fileList);

                            socket.send(packet.getPacket()); 
                            
                    }
                    scan.close();
                    heartBeat();
                    packetNumber++;

                } catch (FileNotFoundException e){
                    System.out.println("The file you inputted does not exist."); //if file input is invalid
                } catch (Exception e){
                    System.out.println("An error has occurred"); //if alternate error has occured
                }
                
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    
    public void receiveMessages() {
        byte[] incomingData = new byte[1024];
        DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
    
        while (true) {
                try {
                    socket.receive(incomingPacket);
    
                    //put hash
                    synchronized(map){

                        map.put(incomingPacket.getAddress(), 0);
                        mapAvailable.put(incomingPacket.getAddress(), " - Alive");
                    }
    
                    OurProtocol deconstructPacket = new OurProtocol(incomingPacket);
                    deconstructPacket.protocolDetails();
                    
                    // String message = new String(incomingPacket.getData()).trim();
                    // System.out.println("Received: " + message);
        
                    // if (message.equals("THEEND")) {
                    //     System.out.println("Termination message received.");
                    //     socket.close();
                    //     executorService.shutdown();
                    //     return;
                    // }
    
                    // System.out.println("Client Details: PORT " + incomingPacket.getPort()
                    //         + ", IP Address: " + incomingPacket.getAddress());
                } catch (IOException  e) {
                    e.printStackTrace();
                }
        }
    }
    

    public void checkHeartbeat(){
        timer.scheduleAtFixedRate(() -> {
        synchronized(map){

            for (Map.Entry<InetAddress, Integer> entry : map.entrySet()){
                if (entry.getValue() >= 30){
                    mapAvailable.put(entry.getKey(), " - Dead");
                }
            }
            System.out.println("Current connections: " + mapAvailable);
        }
        }, 0, 30, TimeUnit.SECONDS);

        timer.scheduleAtFixedRate(() -> {
        synchronized(map){

            for (Map.Entry<InetAddress, Integer> entry : map.entrySet()){
                map.put(entry.getKey(),entry.getValue() + 1);
            }
        }
        }, 0, 1, TimeUnit.SECONDS);

    }

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
            // while (true) {
            //     try {
            //         Thread.sleep(1000);
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }
        });
    }

    public static InetAddress getLocalIPAddress() throws SocketException, UnknownHostException {
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
    }
    

    public static void main(String[] args) {

        try{
            
                
            InetAddress localIPAddress = getLocalIPAddress();
            System.out.println("Local IP Address: " + localIPAddress.getHostAddress());
            System.out.println(localIPAddress);

            PeerToPeer client = new PeerToPeer();
            client.start(localIPAddress);
             
                
 
        }catch(IOException  e){
            e.printStackTrace();
        }
    }
}

/*
 * 10.0.2.15
9876
10.111.103.3
9876
10.111.111.93
9876
10.111.119.140
9876
 */


