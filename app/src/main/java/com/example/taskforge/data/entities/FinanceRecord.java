package com.example.taskforge.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "finance_records",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = {@Index("user_id")})
public class FinanceRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long user_id;
    
    @NonNull
    public String title;
    
    public String description;
    
    @NonNull
    public String record_type; // INCOME, EXPENSE
    
    public String category;
    
    public double amount;
    
    public String currency;
    
    public long date_ms;

    public FinanceRecord(long user_id, @NonNull String title, String description, @NonNull String record_type, String category, double amount, String currency, long date_ms) {
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.record_type = record_type;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.date_ms = date_ms;
    }
}
