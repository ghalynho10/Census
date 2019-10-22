package com.example.census;

public class CustomModelList {
    String image;
    int id;
    String name;
    String cin;

    public CustomModelList(int id, String name, String cin, String image) {
        this.id = id;
        this.name = name;
        this.cin = cin;
        this.image = image;
    }

    public CustomModelList() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getCin() {
        return cin;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }


}



