//package Networking;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class UDPClient 
{
    DatagramSocket Socket;

    private static File getFileListing(){
        File homeDirectory = new File(System.getProperty("user.home"));
        return homeDirectory;
    }

    public UDPClient() 
    {

    }

    public void createAndListenSocket() 
    {
        try 
        {
            Socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
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
            

            //my protocol
            // Integer packetSend = 1;
            // String[] files = {sentence};
            // OurProtocol newPacket = new OurProtocol(IPAddress, Inet4Address.getByName("localhost"), (Integer) 9876, (Integer) 9876, packetSend, files);
            // Socket.send(newPacket.getPacket());

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
        client.createAndListenSocket();
    }
}

