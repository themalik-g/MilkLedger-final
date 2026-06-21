package com.milkledger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private List<Session> sessions;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(Session session);
    }

    public SessionAdapter(List<Session> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session s = sessions.get(position);
        holder.tvName.setText(s.getName() + " (" + s.getYear() + ")");
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(s));
    }

    @Override
    public int getItemCount() { return sessions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSessionName);
        }
    }
}
