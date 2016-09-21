//Jordan Ogilvy, ID 1288323, 20/9/2016
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class ChatClient{
	
	private static final int port = 40202;
	private static final String address = "239.0.202.1";

	public static void main(String[] args){
		
		try{
			//create multicast socket, tell it to listen on the hardcoded address and port
			MulticastSocket m = new MulticastSocket(port);
			InetAddress iaddr = InetAddress.getByName(address);
			m.joinGroup(iaddr);
			//spawn a thread to listen and print out any messages received from the socket
			MessageReceiver receiver = new MessageReceiver(m);
			receiver.start();
			
			//get input from console and send it in a datagram packet to be multicast
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
			while(true){
				String message = inputReader.readLine();
				DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), iaddr, port);
				m.send(packet);
			}
			
		}
		catch(UnknownHostException uhe){
			uhe.printStackTrace();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}

class MessageReceiver extends Thread{
	
	private final MulticastSocket m;
	
	MessageReceiver(MulticastSocket m){
		this.m = m;
	}
	
	@Override
	public void run(){
		while(true){
			try{
			//get the incoming DatagramPacket and store in our own packet, incomingPacket. Blocks until the data is stored.
			byte[] buffer = new byte[10280];	//assuming that the message will not be too big. This is 10kb, should be enough
			DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
			m.receive(incomingPacket);
			//get the data from the packet, convert to a string, and print it out to the console, repeat.
			int nullIndex=0;
			for(int i=0;i<buffer.length;++i){
				if(buffer[i]==0) {
					nullIndex = i;
					break;
				}
			}
			byte[] message = new byte[nullIndex];
			for(int j=0;j<nullIndex;++j){
				message[j] = buffer[j];
			}
			System.out.println(incomingPacket.getAddress().getHostAddress() + ":  " + new String(message, "UTF8"));
			
			}catch(SocketException se){
				se.printStackTrace();
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
	}
}