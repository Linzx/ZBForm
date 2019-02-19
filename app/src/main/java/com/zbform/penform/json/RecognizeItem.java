package com.zbform.penform.json;

public class RecognizeItem {
    String id;
    String type;
    HwData[] stroke;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HwData[] getStroke() {
        return stroke;
    }

    public void setStroke(HwData[] stroke) {
        this.stroke = stroke;
    }
}
