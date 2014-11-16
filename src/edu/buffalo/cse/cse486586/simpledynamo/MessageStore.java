package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageStore extends SQLiteOpenHelper{
private static int version=1;
private static String dbname="Messages";
	public MessageStore(Context context) {
		super(context,dbname , null,version);
		
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		 String createTable = "CREATE TABLE IF NOT EXISTS Messages(key STRING PRIMARY KEY, value STRING)";
		 db.execSQL(createTable);
	    		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	

}
