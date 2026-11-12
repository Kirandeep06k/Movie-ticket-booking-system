package com.mbs.model;

/** Interface: anything that has a price for a number of seats. */
public interface Payable {
    double priceFor(int seats);
}
