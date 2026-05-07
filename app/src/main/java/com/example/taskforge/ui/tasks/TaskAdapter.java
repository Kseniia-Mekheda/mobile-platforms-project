package com.example.taskforge.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTaskTitle.setText(task.title);
        holder.tvTaskPriority.setText("Пріоритет: " + task.priority);
        holder.tvTaskStatus.setText("Статус: " + task.status);

        // TODO: Пізніше реалізувати лісенер на кнопку "Таймер" для відкриття фрагменту таймера
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskPriority, tvTaskStatus;
        Button btnOpenTimer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            btnOpenTimer = itemView.findViewById(R.id.btnOpenTimer);
        }
    }
}
