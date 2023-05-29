plugins {
  id("java-gradle-plugin")
  kotlin("jvm")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}

gradlePlugin {
  plugins {
    create("kcpTest") {
      id = "kcpTest"
      implementationClass = "com.ndhzs.plugin.gradle.KcpTestGradlePlugin"
    }
  }
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
}