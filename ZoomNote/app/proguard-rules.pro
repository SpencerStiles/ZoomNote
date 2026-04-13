# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# R-tree — suppress missing AWT/Swing classes (visualization code not used on Android)
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn com.github.davidmoten.rtree2.Visualizer
-dontwarn com.github.davidmoten.rtree2.ImageSaver
-keep class com.github.davidmoten.rtree2.** { *; }

# RxJava (pulled in by rtree2)
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
