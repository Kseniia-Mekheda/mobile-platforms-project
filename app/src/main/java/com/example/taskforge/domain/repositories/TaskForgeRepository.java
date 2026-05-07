package com.example.taskforge.domain.repositories;

import android.app.Application;

import com.example.taskforge.data.TaskForgeDatabase;
import com.example.taskforge.data.daos.TaskForgeDao;
import com.example.taskforge.data.entities.FinanceRecord;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.data.entities.User;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskForgeRepository {

    private TaskForgeDao taskForgeDao;

    public TaskForgeRepository(Application application) {
        TaskForgeDatabase db = TaskForgeDatabase.getDatabase(application);
        taskForgeDao = db.taskForgeDao();
    }

    // --- User Operations ---

    public long insertUser(User user) {
        Future<Long> future = TaskForgeDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() {
                return taskForgeDao.insertUser(user);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public User getUserByEmail(String email) {
        Future<User> future = TaskForgeDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() {
                return taskForgeDao.getUserByEmail(email);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Project Operations ---

    public long insertProject(Project project) {
        Future<Long> future = TaskForgeDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() {
                return taskForgeDao.insertProject(project);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Project> getProjectsForUser(long userId) {
        Future<List<Project>> future = TaskForgeDatabase.databaseWriteExecutor.submit(new Callable<List<Project>>() {
            @Override
            public List<Project> call() {
                return taskForgeDao.getProjectsForUser(userId);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Task Operations ---

    public long insertTask(Task task) {
        Future<Long> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.insertTask(task));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Task> getTasksForProject(long projectId) {
        Future<List<Task>> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.getTasksForProject(projectId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateTask(Task task) {
        TaskForgeDatabase.databaseWriteExecutor.execute(() -> taskForgeDao.updateTask(task));
    }

    // --- Finance Operations ---

    public long insertFinanceRecord(FinanceRecord record) {
        Future<Long> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.insertFinanceRecord(record));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<FinanceRecord> getFinanceRecordsForUser(long userId) {
        Future<List<FinanceRecord>> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.getFinanceRecordsForUser(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Subscription Operations ---
    
    public long insertSubscription(Subscription subscription) {
        Future<Long> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.insertSubscription(subscription));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Subscription> getSubscriptionsForUser(long userId) {
        Future<List<Subscription>> future = TaskForgeDatabase.databaseWriteExecutor.submit(() -> taskForgeDao.getSubscriptionsForUser(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
