package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
int SERVER_PORT=10000;
String myHash;
String myport;
String successor1;
String successor2;
String predecessor1;
String predecessor2;
String predecessorHash;
volatile String CorrectKey;
String Columns[]={"key","value"};
int algo=SQLiteDatabase.CONFLICT_REPLACE;
private volatile ArrayList<SerialCursorRow>answerCursor;
private Vector<String> Count;
private int Sum=0;
final String []avd_no={"5562","5556","5554","5558","5560"};
TreeMap <String,String>neighborMap;
public static MessageStore messagesdb;
public static SQLiteDatabase db;
ClientTask ct;
Uri mUri;

	
@Override
	public boolean onCreate() {
		answerCursor= new ArrayList<SerialCursorRow>();
   	    Count=new Vector<String>();   
   	    neighborMap=new TreeMap<String,String>();
   	    messagesdb=new MessageStore(this.getContext());
   	    if (messagesdb!=null){
     	   db=messagesdb.getWritableDatabase();
     	   Log.v("Create","Database Created");
     	   
        }
        else
        	return false;
   	    ct=new ClientTask();
   	    for (String x:avd_no){
		  try {
			neighborMap.put(genHash(x),String.valueOf((Integer.parseInt(x)*2)));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	
	    }
   	  try {
		Setup_Ports();
		Socket_create();
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  Log.e("Create","Port-"+myport);
	  String tmp[]=successor_List(myHash);
	  successor1=tmp[0];
	  successor2=tmp[1];
	  predecessor_List();
	  Log.e("Create",successor1+" "+successor2);
	  Log.e("Create",predecessor1+" "+predecessor2);
	  Log.e("Create",myHash+" "+predecessorHash);
	  FetchData();
	  try {
		Thread.sleep(2000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  Log.e("Create","Successful");
  	 return false;
	}

private final void FetchData() {
Message msgpre1=new Message("Replica",myport);
msgpre1.setMyHash(myHash);
msgpre1.setPredecessorHash(predecessorHash);
msgpre1.setDest_port(predecessor1);

Message msgpre2=new Message("Replica",myport);
msgpre2.setDest_port(predecessor2);
msgpre2.setMyHash(myHash);
msgpre2.setPredecessorHash(predecessorHash);

Message msgsucc1=new Message("Origin",myport);
msgsucc1.setDest_port(successor1);
msgsucc1.setMyHash(myHash);
msgsucc1.setPredecessorHash(predecessorHash);

Message msgsucc2=new Message("Origin",myport);
msgsucc2.setDest_port(successor2);
msgsucc2.setMyHash(myHash);
msgsucc2.setPredecessorHash(predecessorHash);

new InitTask().execute(msgpre1);
new InitTask().execute(msgpre2);
new InitTask().execute(msgsucc1);
new InitTask().execute(msgsucc2);

}

private final void predecessor_List(){
	if (myHash.equals(neighborMap.firstKey())){
		predecessor1=neighborMap.get(neighborMap.lastKey());
		predecessorHash=neighborMap.lastKey();
		predecessor2=neighborMap.get(neighborMap.lowerKey(neighborMap.lastKey()));
	}
	else{
		if (myHash.equals(neighborMap.higherKey(neighborMap.firstKey()))){
			predecessor2=neighborMap.get(neighborMap.lastKey());
	    	predecessor1=neighborMap.get(neighborMap.firstKey());
	    	predecessorHash=neighborMap.firstKey();
		}
		else{
			predecessor1=neighborMap.get(neighborMap.lowerKey(myHash));
	    	predecessor2=neighborMap.get(neighborMap.lowerKey(neighborMap.lowerKey(myHash)));
	    	predecessorHash=neighborMap.lowerKey(myHash);
		}
	}


}

private final String[] successor_List(String nodeHash){
		String scr1,scr2;
		if (nodeHash.equals(neighborMap.lastKey())){
    		scr1=neighborMap.get(neighborMap.firstKey());
    		scr2=neighborMap.get(neighborMap.higherKey(neighborMap.firstKey()));
    	}
    	else{
    		if (nodeHash.equals(neighborMap.lowerKey(neighborMap.lastKey()))){
    			scr1=neighborMap.get(neighborMap.higherKey(nodeHash));
    	    	scr2=neighborMap.get(neighborMap.firstKey());
    		}
    		else{
    			scr1=neighborMap.get(neighborMap.higherKey(nodeHash));
    	    	scr2=neighborMap.get(neighborMap.higherKey(neighborMap.higherKey(nodeHash)));
    	    	
    		}
    	}
		String []s={scr1,scr2};
		return s;
	}
	
	private void Socket_create() {
		   try {
		     	ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
		         new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		      } catch (IOException e) 
		     {
		     	Log.e("Provider", "Can't create a ServerSocket");
		         return;
		     }
	}
	
	private void Setup_Ports() throws NumberFormatException, NoSuchAlgorithmException {
		TelephonyManager tel = (TelephonyManager)this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myport = String.valueOf((Integer.parseInt(portStr) * 2));
        myHash=genHash(String.valueOf((Integer.parseInt(portStr))));
		
	}

	
	Uri buildUri(String string, String string2) {
		Uri.Builder uriBuilder = new Uri.Builder();
  	     uriBuilder.authority(string2);
  	     uriBuilder.scheme(string);
  	     return uriBuilder.build();
	}

	
	

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		   db.delete("Messages","1", selectionArgs);
		   Message msgs[]=new Message[4];
			String avd[]={predecessor1,predecessor2,successor1,successor2};
			for (int i=0;i<avd.length;i++){
				msgs[i]=new Message("Delete",myport);
			    msgs[i].setDest_port(avd[i]);
	            msgs[i].setKey("*");
	            ct.send(msgs[i]);	
				
			}
			return 0;
	       }
	       
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	String isCoordinator(String key){
	String hashKey="";
	try {
		hashKey = genHash(key);
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	String destHash=neighborMap.ceilingKey(hashKey);
	if (destHash==null)destHash=neighborMap.firstKey();
	Log.e("AVD",destHash+" "+neighborMap.get(destHash));
	return neighborMap.get(destHash);
	}
		
	
	@Override	
	public Uri insert(Uri uri, ContentValues values) {
	String key=values.getAsString("key");
	String dest=isCoordinator(key);
	Log.e("ToInsert",key);
	if (dest.equals(myport)){
	db.insertWithOnConflict("Messages", null, values, algo);
	Message msg1=new Message("Insert",myport);
	msg1.setKeyValue(values.getAsString("key"), values.getAsString("value"));
	msg1.setDest_port(successor1);	
	ct.send(msg1);
	Message msg2=new Message("Insert",myport);
	msg2.setKeyValue(values.getAsString("key"), values.getAsString("value"));
	msg2.setDest_port(successor2);
	ct.send(msg2);
	Log.e("Inserted",key);
	}
	else{
		Message msg0=new Message("Insert",myport);
		msg0.setKeyValue(values.getAsString("key"), values.getAsString("value"));
		msg0.setDest_port(dest);	
		ct.send(msg0);
	    Log.e("Insert","Coordinator "+key);
		String hashavd = "";
		 for(Map.Entry<String,String> entry: neighborMap.entrySet()){
	            if(dest.equals(entry.getValue())){
	                hashavd = (String) entry.getKey();
	                break; 
	            }
	        }

		
		String succList[]=successor_List(hashavd);
		Log.e("Successor",succList[0]+" "+succList[1]);
		
		
		if (!succList[0].equals(myport)){
		Message msg1=new Message("Insert",myport);
		msg1.setKeyValue(values.getAsString("key"), values.getAsString("value"));
		msg1.setDest_port(succList[0]);	
		ct.send(msg1);
		}
		else{
            
			db.insertWithOnConflict("Messages", null, values,algo);
				
		}
		
		if (!succList[1].equals(myport)){
		Message msg2=new Message("Insert",myport);
		msg2.setKeyValue(values.getAsString("key"), values.getAsString("value"));
		msg2.setDest_port(succList[1]);
		ct.send(msg2);
		}
		else{
			db.insertWithOnConflict("Messages", null, values,algo);
			
		}
		
		
		Log.e("Sent",key);	
		
	}
	return null;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (selection.equals("@")){
		Cursor queryCursor=db.query("Messages",null,null,null,null,null,null);	
		Log.e("Query","@");
		Log.e("Cursorsize", String.valueOf(queryCursor.getCount()));
        return queryCursor;
    	}
		if (selection.equals("*")){
			Log.e("Query","*");
			synchronized(answerCursor){
				answerCursor.clear();
			}
			Cursor queryCursor1=db.query("Messages",null,null,null,null,null,null);
			ArrayList<SerialCursorRow>cursorMap=genList(queryCursor1);
			synchronized(answerCursor){
				answerCursor.clear();
				answerCursor.addAll(cursorMap);
			}
		   
			Message msgs[]=new Message[4];
			String avd[]={predecessor1,predecessor2,successor1,successor2};
			for (int i=0;i<avd.length;i++){
				msgs[i]=new Message("Query",myport);
			    msgs[i].setDest_port(avd[i]);
	            msgs[i].setKey(selection);
	            ct.send(msgs[i]);	
				
			}
			            
            
            
            try {
            synchronized(answerCursor){
	    		answerCursor.wait(3000);
				
	    		MatrixCursor cr=new MatrixCursor(Columns);
	    	
	    		for (int i=0;i<answerCursor.size();i++){
	    			String []columnValues={answerCursor.get(i).getKey(),answerCursor.get(i).getValue()};
	    			if (i==answerCursor.size()-1)
	    			{
	    				Log.e("Row",answerCursor.get(i).getKey()+answerCursor.get(i).getValue());
	    			}
	    			cr.addRow(columnValues);	
	    			}
	    	 		answerCursor.clear();
	    	 		if (cr!=null)Log.e("Cursorsize",String.valueOf(cr.getCount()));
	    	 		return cr;
	    }
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
				
		String key2=selection;
		String dest1=isCoordinator(key2);
		Log.e("Query",selection);
			Log.e("Query","Executed");
			
			synchronized(db){
		    Cursor cursor=db.query("Messages", null, "key='"+selection+"'", null, null, null, null);
		    if (cursor!=null && cursor.getCount()!=0){
				return cursor;	
			}
			}
		
		
			synchronized(answerCursor){
				CorrectKey=selection;
				answerCursor.clear();
			}
			
			Log.e("Query","Sent to-"+dest1);		
			MatrixCursor cr=new MatrixCursor(Columns);
			
			String hashavd = "";
			 for(Map.Entry<String,String> entry: neighborMap.entrySet()){
		            if(dest1.equals(entry.getValue())){
		                hashavd = (String) entry.getKey();
		                break; 
		            }
		        }

			
			String s[]=successor_List(hashavd);
			
			Message msgq1=new Message("Query",myport);
			msgq1.setKey(selection);
			msgq1.setDest_port(dest1);
			
			Message msgq2=new Message("Query",myport);
			msgq2.setKey(selection);
			msgq2.setDest_port(s[0]);
			
			Message msgq3=new Message("Query",myport);
			msgq3.setKey(selection);
			msgq3.setDest_port(s[1]);
			
			ct.send(msgq1);
			ct.send(msgq2);
			ct.send(msgq3);
			
			try {
			    Log.e("Query","Waiting for Answer");
				synchronized(answerCursor){
				answerCursor.wait(3000);
				if (answerCursor.size()!=0){
				Log.e("Row",answerCursor.get(0).getKey()+answerCursor.get(0).getValue());
	    		String []columnValues={answerCursor.get(0).getKey(),answerCursor.get(0).getValue()};
	    		cr.addRow(columnValues);	
	    		cr.moveToFirst();
				}
				}
	    		   	    			
			}
			catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
			return cr;
		
	}

	private ArrayList<SerialCursorRow> genList(Cursor cr) {
		ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
	   	 int keyIndex = cr.getColumnIndex("key");
			 int valueIndex = cr.getColumnIndex("value");
			 int count=cr.getCount();
			 cr.moveToFirst();
			 for (int i=0;i<count;i++){
			 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
			 cursorMap.add(resultCursor);
			 cr.moveToNext();
			 } 
			 return cursorMap;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        @SuppressWarnings("resource")
		Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    
private class ServerTask extends AsyncTask<ServerSocket, Message, Void> {

    private int count=0;

   	private void Delete_Handler(Uri mUri, Message message) throws NoSuchAlgorithmException {
      	 	  int i=db.delete("Messages","1", null);
      	}
      
       
       
       private void Query_Handler(Uri mUri,Message message) throws NoSuchAlgorithmException{
        Cursor queryCursor=null;
  		 if (!message.getKey().equals("*")){
  			 queryCursor=db.query("Messages", null, "key='"+message.getKey()+"'", null, null, null, null);
   	    	 if (queryCursor!=null && queryCursor.getCount()!=0){
   	    	 ArrayList<SerialCursorRow>cursorMap=sendCursor(queryCursor,null);    	  
    		 Message messageresp=new Message("Result",myport);
  	    	 messageresp.setDest_port(message.getmyPort());
  	    	 messageresp.setKey(message.getKey());
  	    	 messageresp.setCursorMap(cursorMap);
  	    	 ct.send(messageresp);
   	    	 }
   		 }
  		 else{
  			 queryCursor=db.query("Messages", null,null, null, null, null, null);
  			 if (queryCursor!=null){
     	         ArrayList<SerialCursorRow>cursorMap=sendCursor(queryCursor,null);    	  
     	    	 Message messageresp=new Message("Result*",myport);
     	    	 messageresp.setDest_port(message.getmyPort());
     	    	 messageresp.setCursorMap(cursorMap);
     	    	 ct.send(messageresp);
     	    	
     	     }
  		 }
  		 }
    	 	 
       private ArrayList<SerialCursorRow>sendCursor(Cursor cr,String hash) throws NoSuchAlgorithmException{
      	 ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
      	 int keyIndex = cr.getColumnIndex("key");
   		 int valueIndex = cr.getColumnIndex("value");
   		 int count=cr.getCount();
   		 cr.moveToFirst();
  		 for (int i=0;i<count;i++){
  		     SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
  			 cursorMap.add(resultCursor);
  			 cr.moveToNext();
  		 }
  		 return cursorMap;
      	
       }
         
    
    
  @Override
protected Void doInBackground(ServerSocket... sockets) {
    ServerSocket serverSocket = sockets[0];
while(true){
    Message message = null;
	  	Socket clientSocket = null;
	  	ObjectInputStream inSocket = null;
		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (clientSocket!=null){
		try {
			
			inSocket = new ObjectInputStream(clientSocket.getInputStream());
		} 
		catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (inSocket!=null)
			try {
				message = (Message)inSocket.readObject();
				inSocket.close();
				clientSocket.close();
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if (message!=null && !message.getmyPort().equals(myport)){
			mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
     		//Log.e("Msg","Rcvd");	 
     		
			if (message.getMessage().equals("Replica")){
	     	    Log.e("RequestPredecessor",message.getmyPort());
				Cursor cr=null;
	     	   ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
	     	   synchronized(db){
	     	    cr=db.query("Messages",null, null, null,null,null,null);
	     	     
	     	     int keyIndex = cr.getColumnIndex("key");
	     		 int valueIndex = cr.getColumnIndex("value");
	     		 int count=cr.getCount();
	     		 cr.moveToFirst();
	     		try {
	     		 for (int i=0;i<count;i++){
	    			String key=cr.getString(keyIndex);
	         		   		
	         		if(
	         				( (genHash(key).compareTo(predecessorHash)>0) && (genHash(key).compareTo(myHash)<=0))
                                           ||
( ( (genHash(key).compareTo(neighborMap.lastKey())>0) || (genHash(key).compareTo(neighborMap.firstKey())<=0) ) &&  (myHash.equals(neighborMap.firstKey())) )
					 )
					 {
					 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
					 cursorMap.add(resultCursor);
					 
					 }
					cr.moveToNext();
	    		 }
	     		}catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	   	    
	     	    }
	     	    if (cursorMap.size()>0){
	     	    Message msgd=new Message("Data",myport);
	     	    msgd.setCursorMap(cursorMap);
	     	    msgd.setDest_port(message.getmyPort());     	    	
	     	    ct.send(msgd);
	     	    }
	     		}
				
			
			
			
			if (message.getMessage().equals("Origin")){
     	    Log.e("RequestSuccessor",message.getmyPort());
			Cursor cr=null;
     	   ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
     	   synchronized(db){
     	    cr=db.query("Messages",null, null, null,null,null,null);
     	     int keyIndex = cr.getColumnIndex("key");
     		 int valueIndex = cr.getColumnIndex("value");
     		 int count=cr.getCount();
     		 cr.moveToFirst();
     		try {
     		 for (int i=0;i<count;i++){
    		 String key=cr.getString(keyIndex);
     		 
     		if (
					( (genHash(key).compareTo(message.getPredecessorHash())>0) && (genHash(key).compareTo(message.getMyHash())<=0))
				                                ||
				    ( ( (genHash(key).compareTo(neighborMap.lastKey())>0) || (genHash(key).compareTo(neighborMap.firstKey())<=0) ) &&  (message.getMyHash().equals(neighborMap.firstKey())) )
				    )  	 
				 {
     			 Log.e("KeySent",key);
				 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
				 cursorMap.add(resultCursor);
				 
				 }
				cr.moveToNext();
     		 }
     		
     		} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
     	   }
     	    if (cursorMap.size()>0){
     	    Message msgd=new Message("Data",myport);
     	    msgd.setCursorMap(cursorMap);
     	    msgd.setDest_port(message.getmyPort());     	    	
     	    ct.send(msgd);
     	    }
     		}
			
     		if (message.getMessage().equals("Data")){
     			Log.e("InsertData","Processing");
     			ArrayList<SerialCursorRow>tmp=message.getCursorMap();
     			ContentValues cv=new ContentValues();
     			for (int i=0;i<tmp.size();i++){
     			cv.put("key", tmp.get(i).getKey());
     			cv.put("value",tmp.get(i).getValue());
     			db.insertWithOnConflict("Messages",null ,cv,algo);
     			Log.e("InsertData",cv.getAsString("key"));
     			cv.clear();
     			}
     			
       		}
		
		
			
			if (message.getMessage().equals("Insert")){
     			ContentValues cv=new ContentValues();
     			cv.put("key", message.getKey());
     			cv.put("value",message.getValue());
     			db.insertWithOnConflict("Messages",null ,cv,algo);
     			Log.e("InsertPort",cv.getAsString("key"));
       		}
     		
     		if (message.getMessage().equals("Query")){
     			try {
     						
     				Log.e("QueryPort",message.getKey());
     				synchronized(db){
     				Query_Handler(mUri,message);
     				}
				
     			} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
     				             			
       		}
     		
     		if (message.getMessage().equals("Delete")){
     			try {
     				Log.e("DeletePort",message.getKey());
     				Delete_Handler(mUri,message);
     			}catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
     		            			
       		}
     		if (message.getMessage().equals("DeleteResult")){
     			Log.e("Count","CountReceived");
     			synchronized(Count){
     			Sum=message.getDeletecount();
     			Count.notify();
     			}
     		}
     		
     		if (message.getMessage().equals("Result")){
     			ArrayList<SerialCursorRow>cursorMap=message.getCursorMap();
            	synchronized(answerCursor){
            	if (message.getKey().equals(CorrectKey)){	
            		Log.e("Received",message.getKey());
            		CorrectKey="h";
            		answerCursor.addAll(cursorMap);
            		answerCursor.notify();
            	}
        		}
                 		
        	 }
     		
			if (message.getMessage().equals("Result*")){
     			Log.e("Result","AnswerReceived");
            	ArrayList<SerialCursorRow>cursorMap=message.getCursorMap();
            	synchronized(answerCursor){
            		answerCursor.addAll(cursorMap);
             	}
            	count++;
            	if (count==neighborMap.size()-1){
            		synchronized(answerCursor){
                		Log.e("Notify",myport);
            			answerCursor.notify();
                		
                		count=0;
                 	}	
            	}
                 		
        	 }
			
			
     		
			 
		 }
		 message=null;
		 }
	     }
         }

         }


private class InitTask extends AsyncTask<Message, Void, Void> {
	
	@Override
        protected Void doInBackground(Message... msgs) {
try {
	 Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
             Integer.parseInt(msgs[0].getDest_port()));
	 if(socket.isConnected())
     {
                    
     ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
     oout.writeObject(msgs[0]);  
     oout.flush();
     Log.e("Message","Sent");
     oout.close();
     socket.close();
      }
   }
    catch(UnknownHostException e) {
        Log.e("Client", "ClientTask UnknownHostException");
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
return null;
}
}
}
