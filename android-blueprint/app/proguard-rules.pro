# --- Kotlin / Moshi (KotlinJsonAdapterFactory uses reflection) ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.**

-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class ** {
  @com.squareup.moshi.FromJson <methods>;
  @com.squareup.moshi.ToJson <methods>;
}

# JSON models and locally persisted snapshots (Moshi)
-keep class paulokat.de.docspellviewer.DocspellAccount { *; }
-keep class paulokat.de.docspellviewer.StoredAccount { *; }
-keep class paulokat.de.docspellviewer.OfflineDocumentMeta { *; }
-keep class paulokat.de.docspellviewer.DocumentDetailContent { *; }
-keep class paulokat.de.docspellviewer.DetailAttachmentRow { *; }
-keep class paulokat.de.docspellviewer.FavoriteDocumentSnapshot { *; }
-keep class paulokat.de.docspellviewer.LoginResponse { *; }
-keep class paulokat.de.docspellviewer.SearchRequestBody { *; }
-keep class paulokat.de.docspellviewer.SearchResponse { *; }
-keep class paulokat.de.docspellviewer.SearchStatsResponse { *; }
-keep class paulokat.de.docspellviewer.ItemGroup { *; }
-keep class paulokat.de.docspellviewer.NamedRef { *; }
-keep class paulokat.de.docspellviewer.AttachmentSummary { *; }
-keep class paulokat.de.docspellviewer.ItemSummary { *; }
-keep class paulokat.de.docspellviewer.ItemDetail { *; }
-keep class paulokat.de.docspellviewer.ItemDetailTag { *; }
-keep class paulokat.de.docspellviewer.ItemCustomField { *; }
-keep class paulokat.de.docspellviewer.CustomFieldListResponse { *; }
-keep class paulokat.de.docspellviewer.CustomFieldDefinition { *; }
-keep class paulokat.de.docspellviewer.TagListResponse { *; }
-keep class paulokat.de.docspellviewer.TagItem { *; }
-keep class paulokat.de.docspellviewer.ReferenceListResponse { *; }
-keep class paulokat.de.docspellviewer.ReferenceItem { *; }

# --- Retrofit ---
-keepattributes RuntimeVisibleAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# --- AndroidX Security Crypto (Tink / errorprone compile-only annotations) ---
-dontwarn com.google.errorprone.annotations.**
-dontwarn androidx.security.crypto.**

# --- BuildConfig ---
-keep class paulokat.de.docspellviewer.BuildConfig { *; }
