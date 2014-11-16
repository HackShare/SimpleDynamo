package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;

public class SerialCursorRow implements Serializable{

	private static final long serialVersionUID = 1L;
private String key;
private String value;

public SerialCursorRow(String key,String value){
	this.setKey(key);
	this.setValue(value);
}

public String getKey() {
	return key;
}

public void setKey(String key) {
	this.key = key;
}

public String getValue() {
	return value;
}

public void setValue(String value) {
	this.value = value;
}

}
