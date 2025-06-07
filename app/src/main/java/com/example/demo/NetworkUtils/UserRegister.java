package com.example.demo.NetworkUtils;

public class UserRegister {
    private String name;
    private String password;
    public UserRegister(String username, String password) {
        this.name = username;
        this.password = password;
    }

    public String getUsername() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
