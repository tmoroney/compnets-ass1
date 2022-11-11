/**
 * @author Thomas Moroney
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class for packet content that represents acknowledgements
 *
 */
public class ReqPacketContent extends PacketContent {

	String info;

	/**
	 * Constructor that takes in information about a file.
	 * @param Info Information about the file.
	 */
	ReqPacketContent(String info) {
		type= REQPACKET; // equal to 10 (value to identify)
		this.info = info;
	}

	/**
	 * Constructs an object out of a datagram packet.
	 * @param packet Packet that contains information about a file.
	 */
	protected ReqPacketContent(ObjectInputStream oin) { // when receive a packet it comes as ObjectInputStream (string)
		try {
			type= REQPACKET; // just assigning
			info= oin.readUTF(); // takes content and turns it back into original form - must change to readbytes etc to read data
		}
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) { // converts to UTF8 for transfer
		try {
			oout.writeUTF(info); // UTF8 encoding
		}
		catch(Exception e) {e.printStackTrace();} //
	}



	/**
	 * Returns the content of the packet as String.
	 *
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		return "REQ:" + info;
	}

	/**
	 * Returns the info contained in the packet.
	 *
	 * @return Returns the info contained in the packet.
	 */
	public String getPacketInfo() {
		return info;
	}
}
