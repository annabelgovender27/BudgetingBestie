package vcmsa.projects.budgetingbestie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


internal class BudgetAdapter(budgetList: List<Budget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {
    private var budgetList: List<Budget>

    init {
        this.budgetList = budgetList
    }

    class BudgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvName: TextView = view.findViewById(R.id.tvName)
        var tvAmount: TextView = view.findViewById(R.id.tvBAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_budget_layout, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        // Bind the budget item at the current position
        val budget: Budget= budgetList[position]
        holder.tvName.text = budget.name
        holder.tvAmount.text = ": R${budget.amount}"
    }

    override fun getItemCount(): Int {
        return budgetList.size // Return the size of the budget list
    }

    fun updateList(newList: List<Budget>) {
        budgetList = newList
        notifyDataSetChanged()
    }
}


