package com.example.taskforge.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String name;
    
    @NonNull
    public String email;
    
    @NonNull
    public String password_hash;

    public User(@NonNull String name, @NonNull String email, @NonNull String password_hash) {
        this.name = name;
        this.email = email;
        this.password_hash = password_hash;
    }
}
