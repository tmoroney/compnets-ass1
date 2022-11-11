/**
 * @author Thomas Moroney
 */

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Ingress extends Node {
    static final int CLIENT_PORT = 50000;
	static final int INGRESS_PORT = 50001;
	static final int WORKER1_PORT = 50002;
	static final int WORKER2_PORT = 50003;
	static final int WORKER3_PORT = 50004;
	static final String CLIENT_NODE = "client";
	static final String WORKER1_NODE = "worker1";
	static final String WORKER2_NODE = "worker2";
	static final String WORKER3_NODE = "worker3";
    InetSocketAddress clientAddress;
	InetSocketAddress workerAddress;
	DatagramPacket request; // global variable to hold request packet
	int workerNum = 1;

	// -------- Add global variable that holds request packet -----------

	/*
	 *
	 */
	Ingress(int port) {
		try {
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");

			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.REQPACKET) {
				request = new ReqPacketContent(((ReqPacketContent)content).getPacketInfo()).toDatagramPacket();
				InetSocketAddress workerAddress = new InetSocketAddress(WORKER1_NODE, WORKER1_PORT);
				switch (workerNum) {
					case 1:
						workerAddress = new InetSocketAddress(WORKER1_NODE, WORKER1_PORT);
						break;
					case 2:
						workerAddress = new InetSocketAddress(WORKER2_NODE, WORKER2_PORT);
						break;
					case 3:
						workerAddress = new InetSocketAddress(WORKER3_NODE, WORKER3_PORT);
						break;
					default:
						workerAddress = new InetSocketAddress(WORKER1_NODE, WORKER1_PORT);
				}
				System.out.println("Forwarded request packet to Worker "+workerNum);
				request.setSocketAddress(workerAddress);
				socket.send(request);
				if (workerNum == 3) { // Round robin system for workers
					workerNum = 1;
				}
				else {
					workerNum++;
				}
			}
			else if (content.getType()==PacketContent.FILEINFO) {
				if (((FileInfoContent)content).getFileName().equals("notfound")) { // filename is "notfound"
					System.out.println("File not found in Worker "+workerNum+", sent 'notfound' packet to Client");
				}
				else {
					System.out.println("Forwarded FileInfo packet to Client");
				}
				clientAddress = new InetSocketAddress(CLIENT_NODE, CLIENT_PORT);
				packet.setSocketAddress(clientAddress);
				socket.send(packet);
			}
			else if (content.getType()==PacketContent.BYTEARRAY) {
				System.out.println("Forwarded FileContent packet to Client");
				clientAddress = new InetSocketAddress(CLIENT_NODE, CLIENT_PORT);
				packet.setSocketAddress(clientAddress);
				socket.send(packet);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}

	/*
	 *
	 */
	public static void main(String[] args) {
		try {
			(new Ingress(INGRESS_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
