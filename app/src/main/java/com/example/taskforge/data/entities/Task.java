package com.example.taskforge.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        foreignKeys = {
            @ForeignKey(entity = Project.class, parentColumns = "id", childColumns = "project_id", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "assignee_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("project_id"), @Index("assignee_id")})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long project_id;
    public Long assignee_id; // Can be null if unassigned
    
    @NonNull
    public String title;
    
    public String category;
    
    @NonNull
    public String priority; // High, Med, Low
    
    @NonNull
    public String status; // ToDo, InProgress, Done
    
    public long due_date_ms;
    public long elapsed_time_ms;

    public Task(long project_id, Long assignee_id, @NonNull String title, String category, @NonNull String priority, @NonNull String status, long due_date_ms, long elapsed_time_ms) {
        this.project_id = project_id;
        this.assignee_id = assignee_id;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.due_date_ms = due_date_ms;
        this.elapsed_time_ms = elapsed_time_ms;
    }
}
