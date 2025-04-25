package com.example.bajaj.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class User {
    private int id;
    private String name;

    @JsonProperty("follows")
    private List<Integer> follows;

    // Getters and setters
}
