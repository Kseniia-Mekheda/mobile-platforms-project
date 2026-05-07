package com.example.taskforge.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "project_members",
        primaryKeys = {"project_id", "user_id"},
        foreignKeys = {
            @ForeignKey(entity = Project.class, parentColumns = "id", childColumns = "project_id", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE)
        })
public class ProjectMember {
    public long project_id;
    public long user_id;

    public ProjectMember(long project_id, long user_id) {
        this.project_id = project_id;
        this.user_id = user_id;
    }
}
