import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("kapt")
  `maven-publish`
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
  kapt("com.google.auto.service:auto-service:1.0.1")
  
  testImplementation(kotlin("test"))
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
}

publishing {
  publications {
    create<MavenPublication>("Kcp") {
      groupId = "com.ndhzs.plugin"
      artifactId = "kcpTest"
      version = "0.0.1"
    }
  }
}

tasks.test {
  useJUnitPlatform()
}

