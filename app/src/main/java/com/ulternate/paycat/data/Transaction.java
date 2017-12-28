package com.ulternate.paycat.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

/**
 * Entity for Room Persistence Library representing the Transaction table.
 *
 * This class defines the column names, getters and setters for a payment
 * transaction record in the database.
 */

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float amount;

    public String description;

    public String category;

    @TypeConverters(Converters.class)
    public Date date;

    /**
     * Public constructor for a Transaction.
     *
     * @param amount: float, the amount for the transaction.
     * @param description: String, description for the transaction.
     * @param category: String, category for the transaction.
     * @param date: Date, date (incl. time) for the transaction.
     */
    public Transaction(float amount, String description, String category,
                       Date date) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }
}
