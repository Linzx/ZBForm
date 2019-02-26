package com.zbform.penform.json;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ZBFormRecgonizeResultInfo {
	String recordUuid;
	String formUuid;
	String formCode;
	String formName;
	String items;
//	@Expose(serialize = false, deserialize = false)
//	List<ZBFormRecognizedItem> itemsList;

	public String getRecordUuid() {
		return recordUuid;
	}

	public void setRecordUuid(String recordUuid) {
		this.recordUuid = recordUuid;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}
}
