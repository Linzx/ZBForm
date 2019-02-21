package com.zbform.penform.json;

import com.google.gson.annotations.Expose;

public class RecognizeItem {
    @Expose(serialize = true, deserialize = true)
    String id;
    @Expose(serialize = true, deserialize = true)
    String type;
    @Expose(serialize = true, deserialize = true)
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
