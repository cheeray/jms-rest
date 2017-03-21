package com.cheeray.cdi;

import java.io.Serializable;

public class DispatchEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Class<?> sourceType;
	private final String methodName;
	private final String payload;
	
	private String msgId;

	public DispatchEvent(final Class<?> sourceType, String methodName, String payload) {
		this.sourceType = sourceType;
		this.methodName = methodName;
		this.payload = payload;
	}

	public Class<?> getSourceType() {
		return sourceType;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getPayload() {
		return payload;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DispatchEvent other = (DispatchEvent) obj;
		if (sourceType == null) {
			if (other.sourceType != null)
				return false;
		} else if (!sourceType.equals(other.sourceType))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DispatchEvent [clazz=" + sourceType + ", methodName=" + methodName
				+ ", payload=" + payload + "]";
	}
}
