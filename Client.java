/**
 * @author Thomas Moroney
 */

import java.net.DatagramSocket;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 *
 * Client class
 *
 * An instance accepts user input
 *
 */
public class Client extends Node {
	static final int CLIENT_PORT = 50000;
	static final int INGRESS_PORT = 50001;
	static final String INGRESS_NODE = "ingress";
	InetSocketAddress ingressAddress;
	//int numOfPackets;
	int fileSizeRead;
	String fileName;
	int fileSize;
	FileWriter output;
	File file;
	String path;
	String filetype;
	byte[] byteArray;
	int packetNum;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(String dstHost, int dstPort, int srcPort) {
		try {
			ingressAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			PacketContent content = PacketContent.fromDatagramPacket(packet);
			
			if (content.getType() == PacketContent.FILEINFO) {
				fileSize = ((FileInfoContent)content).getFileSize();
				fileName = ((FileInfoContent)content).getFileName();
				System.out.println("File name: " + fileName);
				System.out.println("File size: " + fileSize);
				path = "Client_files/" + fileName;
				byteArray = new byte[fileSize];

				if (fileName.equals("notfound")) {
					System.out.println("No worker had the requested file.");
					notify();
				}
				else {
					file = new File(path);
					System.out.println("File path is: " + path); 
					boolean value = file.createNewFile(); // returns false if file already exists
                	if (!value){
                 	   System.out.println("The file already exists");
                	}
                	output = new FileWriter(path);
					packetNum = 0;
				}
			}
			else if (content.getType() == PacketContent.BYTEARRAY) {
				System.out.println("Received packet number: " + (packetNum+1));
				FileContent fileContent = (FileContent)content;
				filetype = fileName.substring(fileName.lastIndexOf(".")+1); // get filetype
				if (filetype.equals("txt")) {
					String s = new String(fileContent.byteArray, StandardCharsets.UTF_8);
					s = s.substring(PADDING, s.length());
					output.write(s); // writes contents of byte array to new file

					fileSizeRead = fileSizeRead + (PACKETSIZE-PADDING);
					if (fileSizeRead >= fileSize) {
						output.close();
						notify();
					}
				}
				else if (filetype.equals("png") || filetype.equals("jpg")) {
					byte[] imgArray = Arrays.copyOfRange(fileContent.byteArray, PADDING, PACKETSIZE); // First few bytes in each packet are reserved for the header
					
					if (fileSizeRead + imgArray.length >= fileSize) { // received all packets to complete file
						int lastPacketSize = (int) (fileSize-fileSizeRead);
						System.arraycopy(imgArray, 0, byteArray, fileSizeRead, lastPacketSize);
						ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
                        BufferedImage bImage = ImageIO.read(bis);
                        ImageIO.write(bImage, filetype, new File(path) );
                        System.out.println("Image created");
						notify();
					}
					else {
						System.arraycopy(imgArray, 0, byteArray, fileSizeRead, imgArray.length);
					}
					fileSizeRead = fileSizeRead + imgArray.length;
				}
				packetNum++;
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * Sender Method
	 *
	 */
	public synchronized void start() throws Exception {
		Scanner input = new Scanner(System.in);
		while (true) {
			System.out.print("What file are you requesting? ");
		    String filename = input.nextLine();
			if (filename.equals("exit")) {
				input.close();
				return;
			}
			else {
				System.out.println("Requested " + filename);
			}
    
		    DatagramPacket request;
            request = new ReqPacketContent(filename).toDatagramPacket();
            request.setSocketAddress(ingressAddress);
            socket.send(request);
		    this.wait();
		}
	}


	/**
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {
			(new Client(INGRESS_NODE, INGRESS_PORT, CLIENT_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
