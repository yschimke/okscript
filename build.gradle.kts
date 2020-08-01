
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.72"
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.28.0"
  id("net.nemerosa.versioning") version "2.12.1"
  id("com.diffplug.gradle.spotless") version "3.28.1"
}

repositories {
  jcenter()
  mavenCentral()
  maven(url = "https://jitpack.io")
}

group = "com.github.yschimke"
description = "OkHttp Kotlin CLI"
version = versioning.info.display

base {
  archivesBaseName = "okscript"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  withType(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.3"
    kotlinOptions.languageVersion = "1.3"

    kotlinOptions.allWarningsAsErrors = false
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
  }
}

tasks {
  withType(Tar::class) {
    compression = Compression.NONE
  }
}

dependencies {
  api("com.github.yschimke:oksocial-output:5.1")
  api("com.github.yschimke:okurl:2.14")
  api("com.squareup.okhttp3:logging-interceptor:4.8.0")
  api("com.squareup.okhttp3:okhttp:4.8.0")
  api("com.squareup.okhttp3:okhttp-brotli:4.8.0")
  api("com.squareup.okhttp3:okhttp-dnsoverhttps:4.8.0")
  api("com.squareup.okhttp3:okhttp-sse:4.8.0")
  api("com.squareup.okhttp3:okhttp-tls:4.8.0")
  api("com.squareup.okio:okio:2.4.3")
  api("com.squareup.moshi:moshi:1.9.3")
  api("com.squareup.moshi:moshi-adapters:1.9.3")
  api("com.squareup.moshi:moshi-kotlin:1.9.3")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.8")
  implementation("org.slf4j:slf4j-api:2.0.0-alpha0")
  implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
  implementation("org.zeroturnaround:zt-exec:1.11")
  implementation("com.formdev:svgSalamander:1.1.2.1")
  implementation("org.jfree:jfreesvg:3.4")
  implementation("info.picocli:picocli:4.4.0")
}

val sourcesJar by tasks.creating(Jar::class) {
  classifier = "sources"
  from(kotlin.sourceSets["main"].kotlin)
}

val javadocJar by tasks.creating(Jar::class) {
  classifier = "javadoc"
  from("$buildDir/javadoc")
}

val jar = tasks["jar"] as org.gradle.jvm.tasks.Jar

spotless {
  kotlinGradle {
    ktlint("0.31.0").userData(mutableMapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    trimTrailingWhitespace()
    endWithNewline()
  }
}

publishing {
  repositories {
    maven(url = "build/repository")
  }

  publications {
    create("mavenJava", MavenPublication::class) {
      from(components["java"])
      artifact(sourcesJar)
    }
  }
}
