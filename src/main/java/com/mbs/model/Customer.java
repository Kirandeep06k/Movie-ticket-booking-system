package com.mbs.model;

public class Customer extends Person {
    private static final long serialVersionUID = 1L;
    private int activeBookings;

    public Customer(int id, String name, String email) { super(id, name, email); }

    @Override
    public String getRole() { return "CUSTOMER"; }

    public int getActiveBookings() { return activeBookings; }
    public void setActiveBookings(int n) { this.activeBookings = n; }
}
