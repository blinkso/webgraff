plugins {
    id("kotlin-kapt")
}

dependencies {
    // Telegraff
    api(project(":webgraff-core"))

    // Spring
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // DevTools
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}