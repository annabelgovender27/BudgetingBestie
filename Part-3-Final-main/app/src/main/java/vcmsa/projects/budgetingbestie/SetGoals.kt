package vcmsa.projects.budgetingbestie

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity



import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetGoals : AppCompatActivity() {

    private lateinit var edtMin: EditText
    private lateinit var edtMax: EditText
    private lateinit var btnSetGoals: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goals)

        // UI references
        edtMin = findViewById(R.id.edtMin)
        edtMax = findViewById(R.id.edtMax)
        btnSetGoals = findViewById(R.id.btnSetGoals)

        // Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSetGoals.setOnClickListener {
            val minText = edtMin.text.toString().trim()
            val maxText = edtMax.text.toString().trim()

            if (minText.isEmpty() || maxText.isEmpty()) {
                Toast.makeText(this, "Please enter both minimum and maximum goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val min = minText.toDoubleOrNull()
            val max = maxText.toDoubleOrNull()

            if (min == null || max == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (min > max) {
                Toast.makeText(this, "Minimum goal cannot be greater than maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = Goals(min = min, max = max, userId = user.uid)

            firestore.collection("goals")
                .add(goal)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goals saved successfully", Toast.LENGTH_SHORT).show()
                    finish() // Go back to previous screen (optional)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save goals: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}