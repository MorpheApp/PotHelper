import java.util.regex.Pattern

plugins {
    alias(libs.plugins.android.application)
    signing
    publishing
}

fun String.toVersionCode(): Int {
    val regex = Pattern.compile("""(\d+)\.(\d+)\.(\d+)(?:-dev\.(\d+))?""")
    val matcher = regex.matcher(this)

    if (!matcher.matches()) {
        throw GradleException("Invalid version name format: $this")
    }

    val major = matcher.group(1).toInt()
    val minor = matcher.group(2).toInt()
    val patch = matcher.group(3).toInt()
    val dev = matcher.group(4)?.toInt() ?: 99

    return major * 100_000_000 + minor * 100_000 + patch * 100 + dev
}

android {
    namespace = "app.morphe.pot.helper"

    defaultConfig {
        applicationId = "app.morphe.pot.helper"
        minSdk = 26
        compileSdk = 35
        //noinspection OldTargetApi
        targetSdk = 35
        multiDexEnabled = false
        versionName = version as String
        versionCode = versionName?.toVersionCode()

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }

        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            outputFileName = "${rootProject.name}-$versionName.apk"
        }
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    buildTypes {
        release {

            val keystoreFile = file("keystore.jks")
            signingConfig = if (keystoreFile.exists()) {
                signingConfigs.create("release") {
                    storeFile = keystoreFile
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEYSTORE_ENTRY_ALIAS")
                    keyPassword = System.getenv("KEYSTORE_ENTRY_PASSWORD")
                }
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.safe.parcel)
}

tasks {
    whenTaskAdded {
        if (name.startsWith("lintVital")) {
            enabled = false
        }
    }

    // Because the signing plugin doesn't support signing APKs, do it manually.
    register("sign") {
        group = "signing"

        dependsOn(build)
    }

    // Needed by gradle-semantic-release-plugin.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    publish {
        dependsOn(build)
        dependsOn("sign")
    }
}
