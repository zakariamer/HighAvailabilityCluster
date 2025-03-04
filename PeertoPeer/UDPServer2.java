//package PeertoPeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPServer2
{
    private DatagramSocket socket = null;
    private ExecutorService executorService;

    public UDPServer2() 
    {
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
    public void createAndListenSocket() 
    {
        try 
        {
        	//incoming data buffer
            byte[] incomingData = new byte[1024];

            while (true) 
            {
            	//create incoming packet
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                System.out.println("Waiting...");
                
                //wait for the packet to arrive and store it in incoming packet
                socket.receive(incomingPacket);

                //submit a task to the thread pool to handle the packet
                executorService.submit(()-> handlePacket(incomingPacket));

                
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
            DatagramPacket replyPacket =
                    new DatagramPacket(data, data.length, IPAddress, port);
            socket.send(replyPacket);
        }
        catch(IOException e){

            e.printStackTrace();
        }
    }
    public static void main(String[] args) 
    {
        UDPServer2 server = new UDPServer2();
        server.createAndListenSocket();
    }
}
