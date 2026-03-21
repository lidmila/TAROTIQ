# Add project specific ProGuard rules here.

# Keep Gson serialized/deserialized classes
-keepclassmembers class com.tarotiq.app.domain.model.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * { @androidx.room.* <fields>; }

# Google Play Billing
-keep class com.android.vending.billing.**

# Credential Manager
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
