# BudgetingBestie


Budgeting Besties is a simple and helpful Android app built with Kotlin that makes it easier for anyone to take control of their personal finances. Instead of just tracking what you spend, this app helps you plan your budget, stay organized with custom spending categories, and most importantly, set financial goals that you can actively work toward. Whether it's saving for a trip or staying within your grocery limit, the app helps you compare your actual expenses against your intended goals in a clear and visual way. The app emphasizes clean design, responsiveness, and ease of use. It uses Room Database (RoomDB) for reliable local data storage, ensuring full offline functionality and persistent data even without internet access. Whether you're just starting your budgeting journey or looking to stay on top of your financial goals, **Budgeting Besties** gives you the tools to plan smarter, spend better, and achieve more.

### Features:

* Create and manage custom budget categories
* Add expenses and assign them to specific categories
* Set personal budgeting goals (minimum and maximum sepnding goals)
* View total spending per category for a selected date range
* Filter expenses using a date range picker to view expenses within a specific date range
* Filter categories using a date range picker to view total amount spent per category within a specific date range
* View your 3 latest transactions and top categories on the home screen
* Visual insights:

  * **Pie chart** displaying total amount spent per category
  * **Bar graph** comparing total expenses to minimum and maximum spending goals
 
* Extra features:
  * **Report page** where users can download a monthly expense report, similar to a bank statement
  * **Gamification features** where users earn badges or rewards based on certain budgeting achievements or criteria
* Sign out securely and return to the login screen

### Prerequisites (Needed to build/run the app):

* Android Studio (version 2022.1 or higher recommended)
* Gradle properly configured (automatically done in Android Studio)
* Kotlin SDK and Jetpack libraries installed
* Emulator or physical Android device for testing

### How to Run:

1. Clone the repository or open it in Android Studio
2. Let Gradle sync the app
3. Build the app
4. Run it on an emulator or Android device

### Security & User Experience

* Secure Sign Out function redirects to login
* Fully offline-capable using Room Database (RoomDB)
* Built with Jetpack components for modern, modular Android architecture

### Design Decisions

* Chose firestore for reliable online storage and full offline support
* Used Jetpack View Binding for safe and efficient UI interactions
* Adopted Material 3 (M3) design for consistency and modern UI

### Contributors:

* Amishka Solomon – ST10295986
* Angenalise Elisha Stephen – ST10291541
* Annabel Govender – ST10271600

### References:

* https://m3.material.io/components/date-pickers/overview
* https://developer.android.com/kotlin/coroutines
* https://developer.android.com/develop/ui/views/layout/recyclerview
* https://m3.material.io/
* https://developer.android.com/topic/libraries/view-binding
* https://www.geeksforgeeks.org/how-to-add-a-pie-chart-into-an-android-application/
* https://www.geeksforgeeks.org/how-to-create-a-barchart-in-android/
* http://developer.android.com/build/android-build-structure
* https://github.com/Danielmartinus/Konfetti
* https://stackoverflow.com/questions/42009079/how-to-display-records-from-a-firebase-database-using-a-condition-with-android
