//package PeertoPeer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

public class UDPClient {

    // hint:Use a concurent hash map(static reference)to create the nodes , store socket and data of other computers

    // hint:Create 3 three threads those 3 are recieve, send/heartbeat and check heartbeat(Determine if the nodes )
    // hint: the fourth thread is main for printing 

    private DatagramSocket socket;
    private int packetNumber = 0;
    private static  ExecutorService executorService;
    private int serverPort = 9876;

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

    public UDPClient() {
        // Scan everything from Ip config and store in hashmap 
        
        try {
            // create the socket assuming the server is listening on port 9876
            socket = new DatagramSocket(9876);

            // create a thread pool with 5 threads
            executorService = Executors.newFixedThreadPool(5);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendMessages(InetAddress IPAddress) {

        // hint:Loop through the other nodes and for each of them send data
        // hint: when  send the message use byteArrayOutputStream and ObjectOutputStream 

        try {
            while (true) {
                String[] fileList = getFileListing();
                OurProtocol newPacket = new OurProtocol(IPAddress, InetAddress.getByName("localhost"), serverPort,
                        serverPort, packetNumber, fileList);

                // read config
                try{
                    File inFile = new File("PeertoPeer/Config.txt");
                    String line = "";
                    Scanner scan = new Scanner(inFile);

                    //checks if this port is this system's 
                    while(scan.hasNextLine()){
                        String ip = scan.nextLine();
                        // if(ip.equals(IPAddress.getHostAddress())){
                        //     scan.nextLine(); //skip this node and it's port
                        // } else {
                            String port = scan.nextLine();
                            OurProtocol packet = new OurProtocol(InetAddress.getByName(line), IPAddress, (Integer) Integer.parseInt(port), serverPort, packetNumber, fileList);
                            socket.send(packet.getPacket()); 
                        // }
                    }

                } catch (FileNotFoundException e){
                    System.out.println("The file you inputted does not exist."); //if file input is invalid
                } catch (Exception e){
                    System.out.println("An error has occurred"); //if alternate error has occured
                }

                heartBeat();
                socket.send(newPacket.getPacket());
                System.out.println("Message sent from client");
                packetNumber++;

                // wait time before sending the next packet
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessages() {
        // hint: Dont make a thread in a thread 
        // hint: make threads loop for ever (Use while true) do this in all threads.

        byte[] incomingData = new byte[1024];
        DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
        

        while(true){
            try {
    
              
                    socket.receive(incomingPacket);
                    handlePacket(incomingPacket);
    
                    // submits a task to the thread pool to handle the packet
                    // executorService.submit(() -> handlePacket(incomingPacket));
    
                    // wait time before receiving the next packet
                    TimeUnit.SECONDS.sleep(1);
                
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    // hint:does nothing for right now 
    // hint: Take what handlepacket is doing and put it in recieve dont create a new thread for it 
    private void handlePacket(DatagramPacket incomingPacket) {
        try {

            // retrieve the data
            String message = new String(incomingPacket.getData());

            // terminate if it is "THEEND" message from the client
            if (message.equals("THEEND")) {
                socket.close();
                executorService.shutdown();
                return;
            }
            System.out.println("Received message from client: " + message);
            System.out.println("Client Details:PORT " + incomingPacket.getPort()
                    + ", IP Address:" + incomingPacket.getAddress());

            // retrieve client socket info and create response packet
            InetAddress IPAddress = incomingPacket.getAddress();
            int port = incomingPacket.getPort();
            String reply = "Thank you for the message";
            byte[] data = reply.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);
            socket.send(replyPacket);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void checkHeartbeat(){
        while(true){
            System.out.println("Checking heartbeat ...");
            heartBeat();
            System.out.println("Possible node failure.");
        }
    }

    public void start(InetAddress IPAddress){
        Runnable[] tasks = {
            this::receiveMessages,
            this::checkHeartbeat
        };
        

        for (Runnable task : tasks) {
            executorService.submit(task);
        }

        // for(InetAddress ipAddress : ipAddress){
        //     executorService.submit(() -> sendMessages(ipAddress));
        // }
    }

    public static void main(String[] args) {

        try{
            //System.out.println(getFileListing());
                
                InetAddress ipAddress = InetAddress.getByName("localhost");
                UDPClient client = new UDPClient();
                client.start(ipAddress);
                
                //keeps main thread alive to print status 
                while(true){
                    System.out.println("Running....");
                    TimeUnit.SECONDS.sleep(10);
    
                }
 
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}


