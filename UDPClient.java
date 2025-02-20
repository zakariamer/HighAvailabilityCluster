package Networking;

import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.sql.Time;

public class UDPClient 
{

    DatagramSocket Socket;

        private static String getFileListing(){
            String filePath = System.getProperty("user.dir");
            File homeDirectory = new File(filePath);

            File[] fileListing = homeDirectory.listFiles();

            if (fileListing == null){
                return "EMPTY";
            }

            StringBuilder sb = new StringBuilder();
            for (File file : fileListing){
                sb.append(file.getName());
                sb.append(", ");
            }

            return sb.toString();
        }

    public static void heartBeat(UDPClient client){
            SecureRandom random = new SecureRandom();
    
            int sec = random.nextInt(31);
            System.out.println("Waiting for " + sec + " seconds");
            try {
                TimeUnit.SECONDS.sleep(sec);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            client.createAndListenSocket();
        }
    
        public UDPClient() 
        {
    
        }
    
        public void createAndListenSocket() 
        {
            try 
            {
                Socket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");
                byte[] incomingData = new byte[1024];
                String sentence = "Viehmann";
                byte[] data = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
                Socket.send(sendPacket);
                System.out.println("Message sent from client");
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                Socket.receive(incomingPacket);
                String response = new String(incomingPacket.getData());
                System.out.println("Response from server:" + response);
                Socket.close();
            }
            catch (UnknownHostException e) 
            {
                e.printStackTrace();
            } 
            catch (SocketException e) 
            {
                e.printStackTrace();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    
        public static void main(String[] args) {
            System.out.println(getFileListing());
            UDPClient client = new UDPClient();

            while (true){
                heartBeat(client);
            }
    }
}

