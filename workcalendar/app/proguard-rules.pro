# Work calendar app specific R8 rules. Libraries (Room, WorkManager, Glance, kotlinx.serialization)
# ship their own consumer rules.

# Keep serializers of the backup and stored JSON models (looked up by the generated code).
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class io.github.ceniorpomidor.workcalendar.domain.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.ceniorpomidor.workcalendar.domain.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.ceniorpomidor.workcalendar.domain.**$$serializer { *; }

# Keep readable stack traces in crash reports.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
