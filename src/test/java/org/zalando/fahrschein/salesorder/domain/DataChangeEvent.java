package org.zalando.fahrschein.salesorder.domain;

public class DataChangeEvent<T> {

	private String dataOp;
	
	private String dataType;
	
	private T data;

	public String getDataOp() {
		return dataOp;
	}
	
	public void setDataOp(String dataOp) {
		this.dataOp = dataOp;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public T getData() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
}
