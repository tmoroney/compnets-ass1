/**
 * @author Thomas Moroney
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class for packet content that represents the contents of the file in a byte array
 *
 */
public class FileContent extends PacketContent {

	byte[] byteArray;

	/**
	 * Constructor that takes in information about a file.
	 * @param byteArray Byte array containing file contents.
	 */
	FileContent(byte[] byteArray) {
		type= BYTEARRAY;
		this.byteArray = byteArray;
	}

	/**
	 * Reads ObjectInputStream into a byte array.
	 * @param oin ObjectInputStream that contains file content.
	 */
	protected FileContent(ObjectInputStream oin) {
		try {
			type= BYTEARRAY;
			for(int i=0; i<byteArray.length; i++) {
				byteArray[i] = oin.readByte();
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			for(int i=0; i<byteArray.length; i++) {
				oout.writeByte(byteArray[i]);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * Returns the content of the packet as String.
	 *
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		String s = new String(byteArray, StandardCharsets.UTF_8);
		System.out.println("USING TO STRING HERE ALERT");
		return s;
	}
}
