plugins {
    id("com.android.application")
}

android {
    namespace = "com.wsd.wappblocker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wsd.wappblocker"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

//    implementation("com.android.support:appcompat-v7:28.0.0")
//    implementation("com.android.support.constraint:constraint-layout:1.1.3")
//    implementation("com.android.support:support-v4:28.0.0")
//    implementation("com.android.support:support-vector-drawable:28.0.0")
//    testImplementation 'junit:junit:4.12'
//    androidTestImplementation 'com.android.support.test:runner:1.0.2'
//    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
//    implementation("com.android.support:recyclerview-v7:28.0.0")
//    implementation("com.android.support:design:28.0.0")
//    implementation("com.android.support:preference-v7:28.0.0")
}