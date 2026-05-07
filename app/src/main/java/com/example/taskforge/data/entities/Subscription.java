package com.example.taskforge.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscriptions",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = {@Index("user_id")})
public class Subscription {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long user_id;
    
    @NonNull
    public String title;
    
    public String description;
    
    public String category;
    
    public double amount;
    
    public String currency;
    
    public long start_date_ms;
    
    public long repeat_interval; // e.g., milliseconds for 1 month
    
    public boolean reminder_enabled;

    public Subscription(long user_id, @NonNull String title, String description, String category, double amount, String currency, long start_date_ms, long repeat_interval, boolean reminder_enabled) {
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.start_date_ms = start_date_ms;
        this.repeat_interval = repeat_interval;
        this.reminder_enabled = reminder_enabled;
    }
}
