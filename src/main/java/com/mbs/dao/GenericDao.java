package com.mbs.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Generic DAO interface (interface + generics). */
public interface GenericDao<T> {
    Optional<T> findById(int id) throws SQLException;
    List<T> findAll() throws SQLException;
    int insert(T item) throws SQLException;      // returns generated id
    boolean update(T item) throws SQLException;
    boolean delete(int id) throws SQLException;
}
