/**
 * @author Thomas Moroney
 */

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Worker3 extends Node {
    static final int WORKER3_PORT = 50004;
	static final int INGRESS_PORT = 50001;
    static final String INGRESS_NODE = "ingress";
    InetSocketAddress ingressAddress;
	/*
	 *
	 */
	Worker3(int port) {
		try {
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets are request packets.
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.REQPACKET) {
				System.out.println("Received request packet");

                String fname;
				String filetype;
		        File file= null;
		        FileInputStream fin= null;
		        FileInfoContent fileInfo;
				double totalPackets;
				int packetLength;
				int lastPacketLength;
		        byte[] buffer= null;
				DatagramPacket contentPacket = null;

		        fname= ((ReqPacketContent)content).getPacketInfo(); // name of file requested
		        file= new File("Worker_files/" + fname);

				ingressAddress = new InetSocketAddress(INGRESS_NODE, INGRESS_PORT);
				
				String input = null;
				if (file.exists()) {
					System.out.println("Found " + fname);
					System.out.print("Do you want to send this file? (yes/no): ");
		            Scanner scanner = new Scanner(System.in);
		            input = scanner.nextLine();
					if (input.equals("no")) {
						System.out.println("File not sent.");
					}
				}
				else {
					System.out.println("Requested file was not found!");
				}
				
				if (file.exists() && input.equals("yes")) { // SEND FILE REQUESTED
					System.out.println("Sending " + fname);
		        	System.out.println("File size: " + file.length());
					
					packetLength = PACKETSIZE-PADDING; // PADDING is the space reserved for the header
					filetype = fname.substring(fname.lastIndexOf(".")+1); // get filetype
					if (filetype.equals("txt")) {
						fileInfo= new FileInfoContent(fname, (int) file.length());
						sendInfoPacket(fileInfo);
                    	buffer= new byte[(int) file.length()];
                    	fin= new FileInputStream(file);
                    	totalPackets = Math.ceil(buffer.length/packetLength);

                    	BufferedInputStream bis = new BufferedInputStream(fin);
                    	for(int i=0; i<=totalPackets; i++){
                    	    byte[] byteArray = new byte[PACKETSIZE];
                    	    if(i==totalPackets){
                    	        lastPacketLength = (int) (buffer.length-((totalPackets-1)*packetLength));
                    	        byteArray = new byte[lastPacketLength];
                    	    }
                    	    int bytesReadIn = bis.read(byteArray, 7, byteArray.length-PADDING);
                    	    System.out.println("Sent FileContent Packet Number: " + (i+1));

                    	    FileContent fileContent = new FileContent(byteArray);
                    	    contentPacket = fileContent.toDatagramPacket();
                    	    contentPacket.setSocketAddress(ingressAddress);
                    	    socket.send(contentPacket);
                    	}
                    fin.close();
					}
					else if (filetype.equals("png") || filetype.equals("jpg")) {
						BufferedImage bImage = ImageIO.read(file);
      					ByteArrayOutputStream bos = new ByteArrayOutputStream();
      					ImageIO.write(bImage, filetype, bos );
      					byte [] byteArray = bos.toByteArray();

						fileInfo= new FileInfoContent(fname, (int) byteArray.length);
						sendInfoPacket(fileInfo);

						totalPackets = Math.ceil(byteArray.length/packetLength);
						System.out.println("Total Packets: " + totalPackets+1);
						for (int i = 0; i <= totalPackets; i++) {
							byte[] tmp = new byte[PACKETSIZE];
							if(i==totalPackets){
								lastPacketLength = (int) (byteArray.length % packetLength);
							    tmp = new byte[lastPacketLength+PADDING];
								System.arraycopy(byteArray, (i * packetLength), tmp, 7, lastPacketLength);
							}
							else {
								System.arraycopy(byteArray, (i * packetLength), tmp, 7, packetLength);
							}
							FileContent fileContent = new FileContent(tmp); // Creates new filecontent using array of bytes from buffer
							contentPacket = fileContent.toDatagramPacket();
							contentPacket.setSocketAddress(ingressAddress);
							socket.send(contentPacket);
							System.out.println("Sent packet number: " + (i+1)); // i starts at 0 so need to add 1 to packet number
						}
					}
				}
				else  { // Send empty packet to let ingress know that no file was found.
					fileInfo= new FileInfoContent("notfound", 0);
		        	sendInfoPacket(fileInfo);
				}
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}

	public void sendInfoPacket(FileInfoContent fileInfo) {
		try {
			DatagramPacket infoPacket= fileInfo.toDatagramPacket();
		    infoPacket.setSocketAddress(ingressAddress);
		    socket.send(infoPacket);    // Send packet with file name and length
		    System.out.println("Sent Info-Packet w/ name & length of file");
		}
		catch(Exception e) {e.printStackTrace();}
	}
		


	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}

	public static void main(String[] args) {
		try {
			(new Worker3(WORKER3_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
    
}
