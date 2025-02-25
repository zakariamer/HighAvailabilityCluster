import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * @authors Morgan, Fahim, Zakaria
 * 
 * Creates a serlializable collection of data that 
 */
public class OurProtocol implements Serializable{
    private String protocolType;

    private InetAddress destinationIP;
    private InetAddress senderIP;

    private Integer destinationPort;
    private Integer senderPort;

    private Integer packetNumber;
    private String[] files;
    private String data;

    private DatagramPacket packet;


    public OurProtocol (InetAddress dIP, InetAddress sIP, Integer dPort, Integer sPort, Integer packetNum, String[] files ){
        this.protocolType = "En-cryptid's UDP";
        this.destinationIP = dIP;
        this.senderIP = sIP;
        this.destinationPort = dPort;
        this.senderPort = sPort;
        this.packetNumber = packetNum;
        this.files = files;

        //packing data into bytes
        //destination ip, sender ip, destination port, sender port, packet #, files
        this.data = this.protocolType + dIP.getHostAddress() + "," + sIP.getHostAddress() + "," + dPort + "," + sPort + "," + packetNum;
        for(String file : files){
            this.data += "," + file;
        }
    }

    public OurProtocol (DatagramPacket packet){
        String unloadData = new String(packet.getData(), 0, packet.getLength());
        String[] dataParts = unloadData.split(",");

        if(dataParts[0].equals ("En-cryptid's UDP")){
            this.protocolType = "En-cryptid's UDP";
            
            try {
                this.destinationIP = (InetAddress) InetAddress.getByName(dataParts[1]);
                this.senderIP = (InetAddress) InetAddress.getByName(dataParts[2]);
            } catch (UnknownHostException e) {
                System.out.println("Error in IP Loading");
                e.printStackTrace();
            }
            
            this.destinationPort = Integer.parseInt(dataParts[3]);
            this.senderPort = Integer.parseInt(dataParts[4]);
            this.packetNumber = Integer.parseInt(dataParts[5]);

            for(int file = 6; file < dataParts.length; file ++){
                this.files[file - 6] = dataParts[file];
            }
            

        } else {
            this.protocolType = "En-cryptid's UDP";
            try {
                this.destinationIP = Inet4Address.getByName("127.0.0.1");
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.senderIP = packet.getAddress();
            this.destinationPort = packet.getPort();
            this.senderPort = packet.getPort();
            this.packetNumber = null;
            this.files[0] = packet.getData().toString();
        }
    }

    public Integer packetNum(){
        return this.packetNumber;
    }

    public String files(){
        return this.files.toString();
    }

    public DatagramPacket packet(){
        return this.packet;
    }

    public void protocolDetails(){
        System.out.println(" | " + "Type:" + "En-cryptid's UDP" + " | " + "Packet #: " + packetNumber + " | ");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println(" | " + "Destination IP:" + this.destinationIP + " | " + "Destination Port:"  + this.destinationPort + " | " + "Sender IP:"  + this.senderIP + " | " + "Sender Port:" + this.senderPort + " | ");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println(files());
    }

    public DatagramPacket getPacket(){
        byte[] sendingInfo = data.getBytes();
        DatagramPacket packet = new DatagramPacket(sendingInfo, sendingInfo.length, destinationIP, destinationPort);
        return packet;
    }

    public String[] deconstructPacket(DatagramPacket packet){
        String unloadData = new String(packet.getData(), 0, packet.getLength());
        String[] dataParts = unloadData.split(",");

        if(dataParts[0].equals ("En-cryptid's UDP")){
            this.protocolType = "En-cryptid's UDP";
            
            try {
                this.destinationIP = (InetAddress) InetAddress.getByName(dataParts[1]);
                this.senderIP = (InetAddress) InetAddress.getByName(dataParts[2]);
            } catch (UnknownHostException e) {
                System.out.println("Error in IP Loading");
                e.printStackTrace();
            }
            
            this.destinationPort = Integer.parseInt(dataParts[3]);
            this.senderPort = Integer.parseInt(dataParts[4]);
            this.packetNumber = Integer.parseInt(dataParts[5]);

            for(int file = 6; file < dataParts.length; file ++){
                this.files[file - 6] = dataParts[file];
            }
            

        }

        return dataParts;
    }

    
}
