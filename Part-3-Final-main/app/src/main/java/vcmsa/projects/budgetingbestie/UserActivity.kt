package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtSurname: EditText
    private lateinit var edtNumber: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        edtName = findViewById(R.id.edtName)
        edtSurname = findViewById(R.id.edtSurname)
        edtNumber = findViewById(R.id.edtNumber)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val name = edtName.text.toString().trim()
        val surname = edtSurname.text.toString().trim()
        val number = edtNumber.text.toString().trim()
        val user = auth.currentUser

        if (name.isEmpty() || surname.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val email = user.email ?: "NoEmailFound"

        val userProfile = hashMapOf(
            "uid" to uid,
            "name" to name,
            "surname" to surname,
            "contactNumber" to number,
            "email" to email
        )

        // Use UID as document ID
        db.collection("users").document(uid)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                edtName.text.clear()
                edtSurname.text.clear()
                edtNumber.text.clear()
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
