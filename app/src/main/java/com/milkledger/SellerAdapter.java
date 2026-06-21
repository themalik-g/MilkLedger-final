package com.milkledger;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.ViewHolder> {
    private List<Seller> sellers;
    private Map<Long, Double> balances;
    private OnSellerActionListener listener;

    public interface OnSellerActionListener {
        void onEdit(Seller seller);
        void onToggleActive(Seller seller);
        void onViewLedger(Seller seller);
    }

    public SellerAdapter(List<Seller> sellers, Map<Long, Double> balances, OnSellerActionListener listener) {
        this.sellers = sellers;
        this.balances = balances;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Seller s = sellers.get(position);
        holder.tvNumber.setText(s.getNumber());
        holder.tvName.setText(s.getName());
        holder.tvContact.setText(s.getContact());

        double balance = balances.containsKey(s.getId()) ? balances.get(s.getId()) : 0.0;
        holder.tvBalance.setText(String.format(java.util.Locale.getDefault(), "%.2f", balance));
        holder.tvBalance.setTextColor(balance >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));

        if (!s.isActive()) {
            holder.itemView.setAlpha(0.6f);
            holder.btnToggle.setText("Activate");
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.btnToggle.setText("Deactivate");
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        holder.btnToggle.setOnClickListener(v -> listener.onToggleActive(s));
        holder.btnLedger.setOnClickListener(v -> listener.onViewLedger(s));
    }

    @Override
    public int getItemCount() { return sellers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvName, tvContact, tvBalance;
        Button btnEdit, btnToggle, btnLedger;
        ViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvName = itemView.findViewById(R.id.tvName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggle = itemView.findViewById(R.id.btnToggle);
            btnLedger = itemView.findViewById(R.id.btnLedger);
        }
    }
}
