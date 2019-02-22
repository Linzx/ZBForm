package com.zbform.penform.json;

public class ModifyPostParams {
    private String formId;
    private String recordId;
    private String itemCode;
    private String itemData;

    public ModifyPostParams(String formId, String recordId, String itemCode, String itemData) {
        this.formId = formId;
        this.recordId = recordId;
        this.itemCode = itemCode;
        this.itemData = itemData;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }
}
