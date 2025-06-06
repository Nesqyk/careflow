package edu.careflow.repository.entities;

import java.sql.Time;

public class User {

    private int user_id; //
    private String password;
    private String username; // 这里存储的是 username
    private int roleId;
    private int staffId;
    private String firstName;
    private String lastName;
    private Time createdAt;

    public enum Role {
        DOCTOR,
        PATIENT,
        ADMIN,
        NURSE
    }

    public User(int user_id, String password, String username, int role, int staffId, String firstName, String lastName) {
        this.user_id = user_id; // 初始化 user_id
        this.password = password;
        this.username = username; // 这里存储的是 username
        this.roleId = role;
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getId() {
        return user_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username; // 这里返回的是 username
    }

    public int getRoleId() {
        return roleId;
    }

    public int getStaffId() {
        return staffId;

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Time getCreatedAt() {
        return createdAt;

    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' + // 这里返回的是 username
                ", roleId=" + roleId +
                ", staffId=" + staffId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}