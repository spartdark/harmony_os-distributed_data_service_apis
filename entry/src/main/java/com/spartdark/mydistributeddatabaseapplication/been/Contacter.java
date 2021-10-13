package com.spartdark.mydistributeddatabaseapplication.been;

public class Contacter {
    private String name;

    private String phone;

    public Contacter(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return this.name;
    }

    public String getPhone() {
        return this.phone;
    }
}
