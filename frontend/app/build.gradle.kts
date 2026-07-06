import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.travel.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.travel.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Legge l'indirizzo IP locale da local.properties (escluso da Git)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { inputStream ->
                localProperties.load(inputStream)
            }
        }
        
        // Legge le variabili dal file .env nella root del progetto
        val envProperties = Properties()
        val envFile = rootProject.file("../.env")
        if (envFile.exists()) {
            envFile.inputStream().use { inputStream ->
                envProperties.load(inputStream)
            }
        }

        val backendUrl = localProperties.getProperty("backend.url") ?: "http://10.0.2.2:8080/"
        buildConfigField("String", "BACKEND_URL", "\"$backendUrl\"")

        val keycloakUrl = localProperties.getProperty("keycloak.url") ?: "http://10.0.2.2:8081/"
        val keycloakRealm = localProperties.getProperty("keycloak.realm") ?: "ae-realm"
        val keycloakClientId = localProperties.getProperty("keycloak.client.id") ?: "ae-client"
        val keycloakClientSecret = localProperties.getProperty("keycloak.client.secret") ?: "travel-dev-secret"

        buildConfigField("String", "KEYCLOAK_URL", "\"$keycloakUrl\"")
        buildConfigField("String", "KEYCLOAK_REALM", "\"$keycloakRealm\"")
        buildConfigField("String", "KEYCLOAK_CLIENT_ID", "\"$keycloakClientId\"")
        buildConfigField("String", "KEYCLOAK_CLIENT_SECRET", "\"$keycloakClientSecret\"")

        val stripePublishableKey = envProperties.getProperty("STRIPE_PUBLISHABLE_KEY") 
            ?: localProperties.getProperty("stripe.publishable.key") 
            ?: "pk_test_4eC39HqLyjWDarjtT1zdp7dc"
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.stripe:stripe-android:20.48.6")
    implementation(project(":common-dtos"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}





