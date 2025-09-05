package vcmsa.projects.budgetingbestie;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private final Context context;

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvCategory, tvDate;
        Button btnEdit;
        ImageView ivReceipt;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivReceipt = itemView.findViewById(R.id.ivReceipt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText("Amount: R" + expense.getAmount());
        holder.tvCategory.setText("Category: " + expense.getCategory());
        holder.tvDate.setText("Date: " + expense.getDate());


        try {
            if (expense.getReceiptPhotoUri() != null && !expense.getReceiptPhotoUri().isEmpty()) {
                holder.ivReceipt.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(Uri.parse(expense.getReceiptPhotoUri()))
                        .into(holder.ivReceipt);
            } else {
                holder.ivReceipt.setVisibility(View.GONE);
            }
        } catch (SecurityException e) {
            holder.ivReceipt.setVisibility(View.GONE);
            Log.e("ExpenseAdapter", "Permission denied for receipt image", e);
        }

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditExpense.class);
            intent.putExtra("expenseId", expense.getId());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newData) {
        this.expenseList = newData;
        notifyDataSetChanged();
    }
}
