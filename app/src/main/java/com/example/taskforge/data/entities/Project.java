package com.example.taskforge.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects",
        foreignKeys = @ForeignKey(entity = User.class, 
                                  parentColumns = "id", 
                                  childColumns = "owner_id", 
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("owner_id")})
public class Project {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long owner_id;
    
    @NonNull
    public String name;
    
    public String description;

    public Project(long owner_id, @NonNull String name, String description) {
        this.owner_id = owner_id;
        this.name = name;
        this.description = description;
    }
}
