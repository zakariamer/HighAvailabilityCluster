//package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPServer {

    DatagramSocket socket = null;

    HashMap<InetAddress,Integer> map = new HashMap<>();
    HashMap<InetAddress,String> mapAvailable = new HashMap<>();

    ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);

    public UDPServer() {

    }

    public void serverTimer(){
        timer.scheduleAtFixedRate(() -> {
            for (Map.Entry<InetAddress, Integer> entry : map.entrySet()){
                if (entry.getValue() >= 30){
                    mapAvailable.put(entry.getKey(), " - Dead");
                }
            }
            System.out.println("Current connections: " + mapAvailable);
        }, 0, 30, TimeUnit.SECONDS);

        timer.scheduleAtFixedRate(() -> {
            for (Map.Entry<InetAddress, Integer> entry : map.entrySet()){
                map.put(entry.getKey(),entry.getValue() + 1);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    public void createAndListenSocket() 
    {
        try 
        {
            socket = new DatagramSocket(9876);
            byte[] incomingData = new byte[1024];

            while (true) 
            {

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                String message = new String(incomingPacket.getData());
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                
                
                System.out.println("Received message from client: " + message);
                System.out.println("Client IP:"+IPAddress.getHostAddress());
                System.out.println("Client port:"+port);

                map.put(IPAddress, 0);
                mapAvailable.put(IPAddress, " - Alive");

                OurProtocol deconstructPacket = new OurProtocol(incomingPacket);
                deconstructPacket.protocolDetails();
                
                String reply = "Thank you for the message";
                byte[] data = reply.getBytes();
                
                DatagramPacket replyPacket =
                        new DatagramPacket(data, data.length, IPAddress, port);
                
                socket.send(replyPacket);
                Thread.sleep(2000);
            }
        } 
        catch (SocketException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException i) 
        {
            i.printStackTrace();
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        UDPServer server = new UDPServer();
        server.serverTimer();
        server.createAndListenSocket();
    }
}

