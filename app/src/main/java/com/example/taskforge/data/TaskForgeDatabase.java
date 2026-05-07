package com.example.taskforge.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.taskforge.data.daos.TaskForgeDao;
import com.example.taskforge.data.entities.FinanceRecord;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.ProjectMember;
import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.data.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        User.class, 
        Project.class, 
        ProjectMember.class, 
        Task.class, 
        FinanceRecord.class, 
        Subscription.class
}, version = 1, exportSchema = false)
public abstract class TaskForgeDatabase extends RoomDatabase {

    public abstract TaskForgeDao taskForgeDao();

    private static volatile TaskForgeDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static TaskForgeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TaskForgeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TaskForgeDatabase.class, "taskforge_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                TaskForgeDao dao = INSTANCE.taskForgeDao();
                
                // Create dummy users
                long userId1 = dao.insertUser(new User("Test User 1", "user1@taskforge.com", "hash_placeholder_1"));
                long userId2 = dao.insertUser(new User("Test User 2", "user2@taskforge.com", "hash_placeholder_2"));

                // Create dummy projects
                long projectId1 = dao.insertProject(new Project(userId1, "App Redesign", "Redesign the UI"));
                long projectId2 = dao.insertProject(new Project(userId1, "Marketing Campaign", "Q3 marketing tasks"));

                // Create dummy tasks
                dao.insertTask(new Task(projectId1, userId1, "Prototype Login", "Design", "High", "ToDo", System.currentTimeMillis() + 86400000, 0));
                dao.insertTask(new Task(projectId1, userId1, "Test API", "Backend", "Med", "InProgress", System.currentTimeMillis() + 172800000, 3600000));
                dao.insertTask(new Task(projectId2, null, "Social Media Posts", "Content", "Low", "Done", System.currentTimeMillis() - 86400000, 0));

                // Create dummy finances
                dao.insertFinanceRecord(new FinanceRecord(userId1, "Salary", "Monthly salary", "INCOME", "Job", 50000.0, "UAH", System.currentTimeMillis() - 400000000));
                dao.insertFinanceRecord(new FinanceRecord(userId1, "Groceries", "Silpo", "EXPENSE", "Food", 1200.50, "UAH", System.currentTimeMillis() - 86400000));

                // Create dummy subscriptions
                dao.insertSubscription(new Subscription(userId1, "Netflix", "Premium Plan", "Entertainment", 350.0, "UAH", System.currentTimeMillis(), 2592000000L, true));
                dao.insertSubscription(new Subscription(userId1, "Spotify", "Duo Plan", "Music", 180.0, "UAH", System.currentTimeMillis(), 2592000000L, true));
            });
        }
    };
}
