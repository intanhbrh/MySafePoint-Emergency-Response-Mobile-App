plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
        namespace = "com.example.mysafepoint"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.mysafepoint"
            minSdk = 21
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation("com.google.firebase:firebase-storage")

        // Location
        implementation("com.google.android.gms:play-services-location:21.0.1")
        implementation("com.google.android.gms:play-services-maps:18.2.0")

        // SMS
        implementation("com.google.android.gms:play-services-auth:20.7.0")

        // UI components
        implementation("com.google.android.material:material:1.11.0")
        implementation("de.hdodenhof:circleimageview:3.1.0")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


}


