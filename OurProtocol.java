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

   /**
    * Creates a packet with the implemented protocol that stores this data into the packet.
    * @param destinationIP Packet's destination IP address
    * @param senderIP Packet's sender IP address
    * @param destinationPort Destination port address
    * @param senderPort Sender port address
    * @param packetNum Which number packet is being sent
    * @param files The data being transfered (files for our purposes)
    */
    public OurProtocol (InetAddress destinationIP, InetAddress senderIP, Integer destinationPort, Integer senderPort, Integer packetNum, String[] files){
        this.protocolType = "En-cryptid's UDP";
        this.destinationIP = destinationIP;
        this.senderIP = senderIP;
        this.destinationPort = destinationPort;
        this.senderPort = senderPort;
        this.packetNumber = packetNum;
        this.files = files;

        //packing the data into a compressable string -- protocol type, destination ip, sender ip, destination port, sender port, packet #, files
        this.data = this.protocolType + destinationIP.getHostAddress() + "," + senderIP.getHostAddress() + "," + destinationPort + "," + senderPort + "," + packetNum;
        for(String file : files){
            this.data += "," + file;
            System.out.println(file);
        }

        packet = new DatagramPacket(data.getBytes(), data.getBytes().length, destinationIP, destinationPort);
    }

    /**
    * Creates a packet with the implemented protocol that stores this data into the packet.
    * @param destinationIP Packet's destination IP address
    * @param senderIP Packet's sender IP address
    * @param destinationPort Destination port address
    * @param senderPort Sender port address
    * @param packetNum Which number packet is being sent
    * @param files The data being transfered (files for our purposes)
    */
    public OurProtocol (InetAddress destinationIP, InetAddress senderIP, Integer destinationPort, Integer senderPort, Integer packetNum, String data ){
        this.protocolType = "En-cryptid's UDP";
        this.destinationIP = destinationIP;
        this.senderIP = senderIP;
        this.destinationPort = destinationPort;
        this.senderPort = senderPort;
        this.packetNumber = packetNum;

        this.files = new String[1];
        this.files[0] = data;

        //packing the data into a compressable string -- protocol type, destination ip, sender ip, destination port, sender port, packet #, files
        this.data = this.protocolType + destinationIP.getHostAddress() + "," + senderIP.getHostAddress() + "," + destinationPort + "," + senderPort + "," + packetNum + "," + data;

        //packet form
        packet = new DatagramPacket(data.getBytes(), data.getBytes().length, destinationIP, destinationPort);

    }

    /**
     * Takes in a packet and formats it into the protocol structure if it is this protocol type (OurProtocol/EncryptidsUDP)
     * @param packet that is an En-cryptid UDP / OurProtocol packet
     */
    public OurProtocol (DatagramPacket packet){
        String unloadData = new String(packet.getData(), 0, packet.getLength());
        String[] dataParts = unloadData.split(",");

        //checks if it is an En-crypted UDP
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

            this.packet = packet;            
             
        } 

        //if not, it uses the packet's given information to
        // else {
        //     this.protocolType = "En-cryptid's UDP";
        //     try {
        //         this.destinationIP = Inet4Address.getByName("127.0.0.1");
        //     } catch (UnknownHostException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        //     this.senderIP = packet.getAddress();
        //     this.destinationPort = packet.getPort();
        //     this.senderPort = packet.getPort();
        //     this.packetNumber = null;
        //     this.files[0] = packet.getData().toString();
        // }
    }

    /**
     * Gets the packet's number
     * @return Integer of packet number
     */
    public Integer packetNum(){
        return this.packetNumber;
    }

    /**
     * Gets the files/data 
     * @return String of files
     */
    public String files(){
        return this.files.toString();
    }

    /**
     * Gets the packet in this protocol's format
     * @return
     */
    public DatagramPacket getPacket(){
        return this.packet;
    }

    /**
     * Prints out the details of this packet's protocol
     */
    public void protocolDetails(){
        System.out.println(" | " + "Type:" + "En-cryptid's UDP" + " | " + "Packet #: " + packetNumber + " | ");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println(" | " + "Destination IP:" + this.destinationIP + " | " + "Destination Port:"  + this.destinationPort + " | " + "Sender IP:"  + this.senderIP + " | " + "Sender Port:" + this.senderPort + " | ");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println(files());
    }

    
}
