package com.example.taskforge.ui.projects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.User;

import java.util.List;

public class ProjectMemberAdapter extends RecyclerView.Adapter<ProjectMemberAdapter.MemberViewHolder> {

    private List<User> members;
    private long loggedInUserId;
    private OnKickClickListener kickListener;

    public interface OnKickClickListener {
        void onKickClick(User user);
    }

    public ProjectMemberAdapter(List<User> members, long loggedInUserId, OnKickClickListener kickListener) {
        this.members = members;
        this.loggedInUserId = loggedInUserId;
        this.kickListener = kickListener;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = members.get(position);
        holder.tvMemberName.setText(user.name);
        holder.tvMemberEmail.setText(user.email);

        if (user.id == loggedInUserId) {
            holder.btnKick.setVisibility(View.GONE);
        } else {
            holder.btnKick.setVisibility(View.VISIBLE);
        }

        holder.btnKick.setOnClickListener(v -> {
            if (kickListener != null) {
                kickListener.onKickClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberEmail;
        ImageButton btnKick;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            btnKick = itemView.findViewById(R.id.btnKick);
        }
    }
}