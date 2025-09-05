package vcmsa.projects.budgetingbestie

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetBudget : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtAmount: EditText
    private lateinit var btnSet: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)


        edtName = findViewById(R.id.edtName)
        edtAmount = findViewById(R.id.edtAmount)
        btnSet = findViewById(R.id.btnSet)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSet.setOnClickListener {
            val name = edtName.text.toString().trim()
            val amountText = edtAmount.text.toString().trim()

            if (name.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please enter both name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val budget = Budget(name = name, amount = amount, userId = user.uid)

            // Store in Firestore
            firestore.collection("budgets")
                .add(budget)
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}