package com.zbform.penform.json;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecognizeItem {
    @Expose(serialize = true, deserialize = true)
    String id;
    @Expose(serialize = true, deserialize = true)
    String type;
    @Expose(serialize = true, deserialize = true)
    HwData[] stroke;

    @Expose(serialize = false, deserialize = false)
    public List<HwData> strokeList = new ArrayList<>();

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

    public List<HwData> getStrokeList() {
        return strokeList;
    }

    public void setStrokeList(List<HwData> strokeList) {
        this.strokeList = strokeList;
    }

    @Override
    public String toString() {
        return "RecognizeItem{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", stroke size=" + stroke.length +
                '}';
    }
}
