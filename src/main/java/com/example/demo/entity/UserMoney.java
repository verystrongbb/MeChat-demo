package com.example.demo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMoney implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private long moneyId;
}
