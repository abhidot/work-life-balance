# Keep CallScreeningService for system binding
-keep class com.worklife.boundary.screening.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
