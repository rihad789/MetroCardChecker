package com.service.scanmetro;

import androidx.annotation.NonNull;

public class Stations_Pojo_Class {
    public String id;
    public String name;

    public Stations_Pojo_Class(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}


