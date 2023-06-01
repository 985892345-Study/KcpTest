plugins {
  id("com.gradle.plugin-publish") version "1.2.0" // https://plugins.gradle.org/docs/publish-plugin
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

dependencies {
  implementation(kotlin("gradle-plugin-api"))
}

version = "0.0.10"
group = "com.g985892345.kcptest"

gradlePlugin {
  plugins {
    create("kcp") {
      id = "com.g985892345.kcptest"
      displayName = "KcpTest"
      description = "KcpTest"
      implementationClass = "com.ndhzs.plugin.gradle.KcpTestGradlePlugin"
    }
  }
}