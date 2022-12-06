import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.kotlin.kapt") version "1.6.20"
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.20"
    id("org.springframework.boot") version "2.3.5.RELEASE"
}

group = "ua.blink.telegraff"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("kotlin-spring")
        plugin("maven-publish")
        plugin("idea")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    java.sourceCompatibility = JavaVersion.VERSION_1_8

    dependencies {
        // Kotlin
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-script-util")
        implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
        implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

        implementation("net.java.dev.jna:jna:5.11.0")
    }

    configure<DependencyManagementExtension> {
        val springBootVersion = "2.7.1"
        imports { mavenBom("org.springframework.boot:spring-boot-dependencies:${springBootVersion}") }
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")

    // IDEA
    idea {
        module {
            val kaptMain = file("build/generated/source/kapt/main")
            sourceDirs.add(kaptMain)
            generatedSourceDirs.add(kaptMain)
        }
    }

    // Jar
    tasks.getByName<Jar>("jar") {
        enabled = true
        manifest {
            attributes["Implementation-Version"] = archiveVersion
        }
        archiveFileName.set(rootProject.name + ".jar")
    }

    tasks {
        // Kotlin
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "1.8"
            }
        }
        compileTestKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "1.8"
            }
        }

        val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        artifacts {
            add("archives", sourcesJar)
        }

        bootJar {
            enabled = false
        }
    }
}

tasks.bootJar {
    enabled = false
}