package com.ulternate.paycat.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object to access Transaction records from the AppDatabase.
 *
 * Defines the queries for accessing data as well as inserting/updating records.
 */

@Dao
public interface TransactionDao {

    // Insert methods.
    // These methods return either an array or single rowId for the new records.

    @Insert
    long[] insertTransactions(Transaction[] transactions);

    @Insert
    long insertTransaction(Transaction transaction);

    // Update methods.

    @Update
    void updateTransactions(Transaction[] transactions);

    @Update
    void updateTransaction(Transaction transaction);

    // Delete methods.

    @Delete
    void deleteTransactions(Transaction[] transactions);

    @Delete
    void deleteTransaction(Transaction transaction);

    // Queries.

    // Return transaction record by id.
    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    // Return all transaction records.
    @Query("SELECT * FROM transactions")
    LiveData<List<Transaction>> getTransactions();

    // Return all transactions of a particular category.
    @Query("SELECT * FROM transactions WHERE category = :category")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    // Return all transactions between a date range.
    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to")
    LiveData<List<Transaction>> getTransactionsBetweenDates(Date from, Date to);
}
