# Consumer ProGuard rules for core:data module

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep database class
-keep class * extends androidx.room.RoomDatabase { *; }
