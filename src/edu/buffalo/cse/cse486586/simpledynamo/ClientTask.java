package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class ClientTask {

	public void send(Message msg){
		try {
        	Log.e("Sending",msg.getDest_port());
        	Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
        			Integer.parseInt(msg.getDest_port()));
	 	            	
        	ObjectOutputStream outSocket=new ObjectOutputStream(socket.getOutputStream());
            outSocket.writeObject(msg);
            outSocket.flush();
            outSocket.close();
            socket.close();
            }
    	    catch (UnknownHostException e) {
	            Log.e("Error", "ClientTask UnknownHostException");
	        } catch (IOException e) {
	            Log.e("Error", "ClientTask socket IOException");
	            
	        }        	
        
      }
	}
	
