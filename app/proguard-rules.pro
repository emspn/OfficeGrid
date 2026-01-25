# Room
-keep class androidx.room.** { *; }
-keep class com.app.officegrid.**.*Entity { *; }
-keep class com.app.officegrid.**.*Dao { *; }

# Supabase & Kotlinx Serialization
-keep class io.github.jan.supabase.** { *; }
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepnames class kotlinx.serialization.json.** { *; }
-keepclassmembers class com.app.officegrid.** {
    @kotlinx.serialization.Serializable *;
}
-keep class com.app.officegrid.**.dto.** { *; }

# Domain Models (pure Kotlin)
-keep class com.app.officegrid.**.domain.model.** { *; }

# UI State & Events
-keep class com.app.officegrid.core.ui.UiState { *; }
-keep class com.app.officegrid.core.ui.UiEvent { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class com.app.officegrid.**_HiltComponents* { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Prevent obfuscation of enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}