package com.example.taskforge.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import com.example.taskforge.data.entities.User;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.FinanceRecord;
import com.example.taskforge.data.entities.Subscription;

@Dao
public interface TaskForgeDao {
    
    // User CRUD
    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // Project CRUD
    @Insert
    long insertProject(Project project);

    @Query("SELECT * FROM projects WHERE owner_id = :userId")
    List<Project> getProjectsForUser(long userId);

    // Task CRUD
    @Insert
    long insertTask(Task task);

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY priority, due_date_ms ASC")
    List<Task> getTasksForProject(long projectId);

    @Update
    void updateTask(Task task);

    // Finance CRUD
    @Insert
    long insertFinanceRecord(FinanceRecord record);

    @Query("SELECT * FROM finance_records WHERE user_id = :userId ORDER BY date_ms DESC")
    List<FinanceRecord> getFinanceRecordsForUser(long userId);

    // Subscription CRUD
    @Insert
    long insertSubscription(Subscription subscription);

    @Query("SELECT * FROM subscriptions WHERE user_id = :userId")
    List<Subscription> getSubscriptionsForUser(long userId);
}
