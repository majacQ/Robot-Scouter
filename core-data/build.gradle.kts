import org.jetbrains.kotlin.gradle.internal.CacheImplementation

apply(from = "../standard-android-config.gradle")
plugins {
    id("com.android.library")
    id("kotlin-android-extensions")
}

androidExtensions {
    defaultCacheImplementation = CacheImplementation.NONE
}

dependencies {
    api(project(":core"))
    api(project(":core-model"))

    api(Config.Libs.Firebase.firestore)
    api(Config.Libs.Firebase.auth)
    api(Config.Libs.Firebase.indexing)

    api(Config.Libs.Arch.extensions)
    api(Config.Libs.Arch.common)

    implementation(Config.Libs.Anko.common)
    implementation(Config.Libs.Support.appCompat) { isTransitive = false }
    implementation(Config.Libs.Support.pref) { isTransitive = false }
    implementation(Config.Libs.Miscellaneous.glide) { isTransitive = false }

    implementation(Config.Libs.PlayServices.auth) { isTransitive = false }
    implementation(Config.Libs.FirebaseUi.firestore) { exclude(module = "recyclerview-v7") }
    implementation(Config.Libs.Firebase.storage)
    implementation(Config.Libs.Firebase.config)
    implementation(Config.Libs.Firebase.jobs)

    implementation(Config.Libs.Miscellaneous.retrofit)
    implementation(Config.Libs.Miscellaneous.retrofitGson)
}
