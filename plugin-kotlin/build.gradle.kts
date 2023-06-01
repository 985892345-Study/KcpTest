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

tasks.register("javadocJar", Jar::class.java) {
  archiveClassifier.set("javadoc")
  from("javadoc")
}

tasks.register("sourcesJar", Jar::class.java) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

publishing {
  publications {
    create<MavenPublication>("Kcp") {
      groupId = "com.g985892345.kcptest"
      artifactId = "KcpTest"
      version = "0.0.10"
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
      from(components["java"])
    }
  }
}

tasks.test {
  useJUnitPlatform()
}

