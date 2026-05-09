package com.example.taskforge.ui.projects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;
    private OnProjectClickListener listener;
    private OnProjectEditClickListener editListener;
    private long loggedInUserId;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public interface OnProjectEditClickListener {
        void onProjectEditClick(long projectId);
    }

    public ProjectAdapter(List<Project> projectList, long loggedInUserId, OnProjectClickListener listener, OnProjectEditClickListener editListener) {
        this.projectList = projectList;
        this.loggedInUserId = loggedInUserId;
        this.listener = listener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.tvProjectName.setText(project.name);
        holder.tvProjectDesc.setText(project.description != null ? project.description : "Немає опису");
        holder.tvProjectMembers.setText("Учасників: ...");

        if (project.owner_id == loggedInUserId) {
            holder.btnEditProject.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditProject.setVisibility(View.GONE);
        }

        holder.btnEditProject.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onProjectEditClick(project.id);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public void setProjects(List<Project> projects) {
        this.projectList = projects;
        notifyDataSetChanged();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvProjectDesc, tvProjectMembers;
        ImageButton btnEditProject;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectDesc = itemView.findViewById(R.id.tvProjectDesc);
            tvProjectMembers = itemView.findViewById(R.id.tvProjectMembers);
            btnEditProject = itemView.findViewById(R.id.btnEditProject);
        }
    }
}
