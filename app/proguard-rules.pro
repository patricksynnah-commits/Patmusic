# Add project specific ProGuard rules here.
-keep class com.pat.patmusic.model.** { *; }
-keep class com.pat.patmusic.service.** { *; }
-keepclassmembers class * extends android.media.MediaPlayer { *; }
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-dontwarn com.bumptech.glide.**
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
