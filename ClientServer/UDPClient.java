//package ClientServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

public class UDPClient {

    private DatagramSocket Socket;
    private int packetNumber = 0;

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

    public UDPClient() {}

    public void createAndListenSocket() {
        try {
            while (true) {
                Socket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");
                byte[] incomingData = new byte[1024];

                // Create and send a packet
                String[] fileList = getFileListing();
                OurProtocol newPacket = new OurProtocol(IPAddress, Inet4Address.getByName("localhost"), 9876, 9876, packetNumber++, fileList);

                heartBeat();
                Socket.send(newPacket.getPacket());  
                System.out.println("Message sent from client");

                // Receive response from server
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                Socket.receive(incomingPacket);
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

    public static void main(String[] args) {
        System.out.println(getFileListing());
        UDPClient client = new UDPClient();
        client.createAndListenSocket();       
    }
}
