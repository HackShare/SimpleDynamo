package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.ArrayList;


public class Message implements Serializable{

private static final long serialVersionUID = 1L;
private String message;
private String myPort;
private String key;
private String myHash;
private String PredecessorHash;
private String value;
private String Dest_port;
private ArrayList<SerialCursorRow>cursorMap;
private int deletecount;

public Message (String message,String myPort){
	this.myPort=myPort;
	this.message=message;
		
}	 

public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

public String getmyPort() {
	return myPort;
}

public String getKey() {
	return key;
}

public String getValue() {
	return value;
}


public void setKeyValue(String key,String value) {
	this.key = key;
	this.value=value;
}

public String getDest_port() {
	return Dest_port;
}

public void setDest_port(String Dest_port) {
	this.Dest_port = Dest_port;
}

public void setKey(String selection) {

    this.key=selection;	
}


public ArrayList<SerialCursorRow> getCursorMap() {
	return cursorMap;
}

public void setCursorMap(ArrayList<SerialCursorRow> cursorMap) {
	this.cursorMap = cursorMap;
}

public int getDeletecount() {
	return deletecount;
}

public void setDeletecount(int deletecount) {
	this.deletecount = deletecount;
}

public String getMyHash() {
	return myHash;
}

public void setMyHash(String myHash) {
	this.myHash = myHash;
}

public String getPredecessorHash() {
	return PredecessorHash;
}

public void setPredecessorHash(String predecessorHash) {
	this.PredecessorHash = predecessorHash;
}

}
