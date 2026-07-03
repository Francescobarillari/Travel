plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
}
