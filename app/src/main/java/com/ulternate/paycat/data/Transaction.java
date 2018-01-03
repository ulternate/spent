package com.ulternate.paycat.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity for Room Persistence Library representing the Transaction table.
 *
 * This class defines the column names, getters and setters for a payment
 * transaction record in the database.
 */

@Entity(tableName = "transactions")
public class Transaction implements Serializable{

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float amount;

    public String description;

    public String originalDescription;

    public String category;

    public String paymentApp;

    @TypeConverters(Converters.class)
    public Date date;

    /**
     * Public constructor for a Transaction.
     * @param amount : float, the amount for the transaction.
     * @param description : String, description for the transaction, this is the description field
     *                    edited by the User.
     * @param originalDescription : String, the original description for the transaction (i.e. from
 *                            the merchant).
     * @param category : String, category for the transaction.
     * @param paymentApp : String, the application which sent the original notification.
     * @param date : Date, date (incl. time) for the transaction.
     */
    public Transaction(float amount, String description, String originalDescription, String category,
                       String paymentApp, Date date) {
        this.amount = amount;
        this.description = description;
        this.originalDescription = originalDescription;
        this.category = category;
        this.paymentApp = paymentApp;
        this.date = date;
    }
}
