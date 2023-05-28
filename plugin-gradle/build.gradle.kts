plugins {
  id("java-gradle-plugin")
  kotlin("jvm")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
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