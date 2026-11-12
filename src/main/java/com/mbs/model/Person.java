package com.mbs.model;

import java.io.Serializable;
import java.util.Objects;

/** Abstract base class for all users (inheritance + abstract methods). */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int instanceCount = 0;   // static member: shared by all objects

    protected int id;                       // protected: visible to subclasses
    private String name;                    // private: accessed via getters
    private String email;

    protected Person(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        instanceCount++;
    }

    public abstract String getRole();

    public static int getInstanceCount() { return instanceCount; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person p = (Person) o;
        return id == p.id && Objects.equals(email, p.email);
    }

    @Override
    public int hashCode() { return Objects.hash(id, email); }

    @Override
    public String toString() { return name + " (" + getRole() + ")"; }
}
