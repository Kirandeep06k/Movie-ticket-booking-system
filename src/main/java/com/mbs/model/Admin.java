package com.mbs.model;

public class Admin extends Person {
    private static final long serialVersionUID = 1L;

    public Admin(int id, String name, String email) { super(id, name, email); }

    @Override
    public String getRole() { return "ADMIN"; }
}
