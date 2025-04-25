package com.example.bajaj.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class WebhookResponse {
    private String webhook;
    @JsonProperty("accessToken")
    private String accessToken;
    private Data data;

    public static class Data {
        private List<User> users;

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }
    }

    public String getWebhook() {
        return webhook;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Data getData() {
        return data;
    }
}
