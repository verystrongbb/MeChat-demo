package com.example.demo.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class MyUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;
}
