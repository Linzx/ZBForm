
package com.zbform.penform.db;

import com.google.gson.annotations.Expose;

public abstract class EntityBase {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
