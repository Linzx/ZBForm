package com.zbform.penform.json;

import com.google.gson.annotations.Expose;

public class ZBFormRecognizedItem {
	String code;
	String fieldName;
	String recognizedData;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRecognizedData() {
		return recognizedData;
	}

	public void setRecognizedData(String recognizedData) {
		this.recognizedData = recognizedData;
	}
}
