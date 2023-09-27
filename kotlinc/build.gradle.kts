plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "org.jetbrains.kotlin"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20-Beta2")
    implementation("io.github.itsaky:nb-javac-android:17.0.0.3")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
    implementation("org.jdom:jdom:2.0.2")

    implementation(projects.jaxp)
    api(files("libs/kotlin-compiler-1.9.0-RC.jar"))

    compileOnly(projects.theUnsafe)
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.cosmic.ide"
            artifactId = "kotlinc"
            version = "1.9.0-RC"

            pom {
                name.set("Kotlin Compiler")
                description.set("A port of the Kotlin Compiler to Android")
                url.set("http://github.com/Cosmic-IDE/kotlinc")
                licenses {
                    license {
                        name.set("GNU GPL v3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("pranavpurwar")
                        name.set("Pranav Purwar")
                        email.set("purwarpranav80@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Cosmic-IDE/kotlinc.git")
                    developerConnection.set("scm:git:ssh://github.com/Cosmic-IDE/kotlinc.git")
                    url.set("http://github.com/Cosmic-IDE/kotlinc")
                }
            }

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

