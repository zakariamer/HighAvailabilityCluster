//package PeertoPeer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

public class UDPClient {

    private DatagramSocket socket;
    private int packetNumber = 0;
    private ExecutorService executorService;
    private InetAddress IPAddress;
    private int serverPort = 9876;

    private static String[] getFileListing() {
        String filePath = System.getProperty("user.dir");
        File homeDirectory = new File(filePath);
        File[] fileListing = homeDirectory.listFiles();

        if (fileListing == null) {
            return new String[]{"EMPTY"};
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
        try 
    	{
    		//create the socket assuming the server is listening on port 9876
			socket = new DatagramSocket(9876);

            //create a thread pool with 5 threads
            executorService = Executors.newFixedThreadPool(5);
		} 
    	catch (SocketException e) 
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // public void sendMessages(){
    //     try{
    //         while(true){
    //             String[] fileList = getFileListing();
    //             OurProtocol newPacket = new OurProtocol(IPAddress, InetAddress.getByName("localhost"), serverPort, serverPort, packetNumber++, fileList);

    //             heartBeat();
    //             socket.send(newPacket.getPacket());
    //             System.out.println("Message sent from client");

    //             // wait time before sending the packet 
    //             TimeUnit.SECONDS.sleep(1);
    //         }
    //     }catch(IOException | InterruptedException e){
    //         e.printStackTrace();
    //     }
    // }

    // public void receiveMessages(){
    //     try{
    //         byte[] incomingData = new byte[1024];

    //         while(true){
    //             DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
    //             socket.receive(incomingPacket);

    //             //submits a task to the thread pool to handle the packet
    //             executorService.submit(()-> handlePacket(incomingPacket));
                
    //         }
    //         } catch (IOException e){
                
    //             e.printStackTrace();
    //         }
    //     }

    public void createAndListenSocket() {
        try {
            while (true) {
                socket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");
                byte[] incomingData = new byte[1024];

                // Create and send a packet
                String[] fileList = getFileListing();
                OurProtocol newPacket = new OurProtocol(IPAddress, Inet4Address.getByName("localhost"), 9876, 9876, packetNumber++, fileList);

                heartBeat();
                socket.send(newPacket.getPacket());  
                System.out.println("Message sent from client");

                // Receive response from server
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);

                //submit a task to the thread pool to handle the packet
                executorService.submit(()-> handlePacket(incomingPacket));

                String response = new String(incomingPacket.getData());
                System.out.println("Response from server: " + response);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePacket(DatagramPacket incomingPacket){
        try{

            //retrieve the data
            String message = new String(incomingPacket.getData());
            
            //terminate if it is "THEEND" message from the client
            if(message.equals("THEEND"))
            {
                socket.close();
                executorService.shutdown();
                return;
            }
            System.out.println("Received message from client: " + message);
            System.out.println("Client Details:PORT " + incomingPacket.getPort()
            + ", IP Address:" + incomingPacket.getAddress());
            
            //retrieve client socket info and create response packet
            InetAddress IPAddress = incomingPacket.getAddress();
            int port = incomingPacket.getPort();
            String reply = "Thank you for the message";
            byte[] data = reply.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);
            socket.send(replyPacket);
        }
        catch(IOException e){

            e.printStackTrace();
        }
    }




    public static void main(String[] args) {
        System.out.println(getFileListing());
        UDPClient client = new UDPClient();
        //client.createAndListenSocket();

        
        
        // // create seperate threads for sending and receiving messages 
        // Thread sendThread = new Thread(client:: sendMessages);
        // Thread receiveThread =  new Thread(client:: receiveMessages);

        // sendThread.start();
        // receiveThread.start();

        // try{
        //     sendThread.join();
        //     receiveThread.join();
        // } catch(InterruptedException e) {
        //     e.printStackTrace();

        // }

        try {
            List<InetAddress> ipAddresses = new ArrayList<>();
            ipAddresses.add(InetAddress.getByName("192.168.1.1"));
            ipAddresses.add(InetAddress.getByName("192.168.1.2"));
            ipAddresses.add(InetAddress.getByName("192.168.1.3"));
            ipAddresses.add(InetAddress.getByName("192.168.1.4"));
            ipAddresses.add(InetAddress.getByName("192.168.1.5"));

            UDPClient client = new UDPClient(ipAddresses);

            // Create separate threads for sending messages to each IP address
            for (InetAddress ipAddress : ipAddresses) {
                Thread sendThread = new Thread(() -> client.sendMessages(ipAddress));
                sendThread.start();
            }

            // Create a thread for receiving messages
            Thread receiveThread = new Thread(client::receiveMessages);
            receiveThread.start();

            // Wait for all threads to complete
            for (InetAddress ipAddress : ipAddresses) {
                Thread sendThread = new Thread(() -> client.sendMessages(ipAddress));
                sendThread.join();
            }
            receiveThread.join();
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
