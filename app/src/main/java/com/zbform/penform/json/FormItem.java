package com.zbform.penform.json;

public class FormItem{
    private int form;
    private int item;
    private int page;
    private String type;
    private double locaX;
    private double locaY;
    private double locaH;
    private double locaW;
    private String requiredFlag;
    private String identityFlag;
    private String fieldName;

    public int getForm() {
        return form;
    }

    public void setForm(int form) {
        this.form = form;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLocaX() {
        return locaX;
    }

    public void setLocaX(double locaX) {
        this.locaX = locaX;
    }

    public double getLocaY() {
        return locaY;
    }

    public void setLocaY(double locaY) {
        this.locaY = locaY;
    }

    public double getLocaH() {
        return locaH;
    }

    public void setLocaH(double locaH) {
        this.locaH = locaH;
    }

    public double getLocaW() {
        return locaW;
    }

    public void setLocaW(double locaW) {
        this.locaW = locaW;
    }

    public String getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(String requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public String getIdentityFlag() {
        return identityFlag;
    }

    public void setIdentityFlag(String identityFlag) {
        this.identityFlag = identityFlag;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}