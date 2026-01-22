package com.example.cocacola.models;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role; // "admin" or "customer"
    private String selectedBranch;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSelectedBranch() { return selectedBranch; }
    public void setSelectedBranch(String selectedBranch) { this.selectedBranch = selectedBranch; }
}
