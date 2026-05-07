package com.example.taskforge.domain.utils;

import com.example.taskforge.data.entities.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskSorter {

    /**
     * Сортуємо список завдань за пріоритетом (High -> Med -> Low),
     * а при однаковому пріоритеті - за датою завершення (due_date_ms).
     */
    public static void sortByPriorityAndDate(List<Task> tasks) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                int p1 = getPriorityWeight(t1.priority);
                int p2 = getPriorityWeight(t2.priority);

                if (p1 != p2) {
                    // Вищий пріоритет (менше значення ваги) йде першим
                    return Integer.compare(p1, p2);
                } else {
                    // Якщо пріоритети однакові, сортуємо за датою
                    return Long.compare(t1.due_date_ms, t2.due_date_ms);
                }
            }
        });
    }

    /**
     * Допоміжний метод для конвертації рядка пріоритету у вагу.
     * High = 1 (найвищий), Med = 2, Low = 3.
     */
    private static int getPriorityWeight(String priority) {
        if (priority == null) return 3;
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "med":
            case "medium":
                return 2;
            case "low":
            default:
                return 3;
        }
    }
}
