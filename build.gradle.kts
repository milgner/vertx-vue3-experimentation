import com.github.gradle.node.npm.task.NpxTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

plugins {
  kotlin ("jvm") version "1.4.21"
  application
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("com.github.node-gradle.node") version "3.0.1"
}

group = "net.ilunis.rpg"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val vertxVersion = "4.0.2"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "net.ilunis.rpg.core.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web-templ-jade")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-shell")
  implementation("io.vertx:vertx-sockjs-service-proxy")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

node {
  nodeProjectDir.set(file("src/webapp"))
  npmInstallCommand.set(if (getenv("CI") != null) { "ci" } else { "install" })
  download.set(false)
}

tasks.register<NpxTask>("buildFrontEnd") {
  dependsOn("npmInstall")
  inputs.files(fileTree("src/webapp/src"))
  outputs.dir("build/resources/main/webapp")
  command.set("vite")
  args.set(listOf("build", "--mode", "production"))
}


tasks.named("build") {
  dependsOn("buildFrontEnd")
}
tasks.named("assemble") {
  dependsOn("buildFrontEnd")
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

//configure<org.gradle.jvm.tasks.Jar>("jar") {
//  doLast {
//    println("HENLO!!")
//  }
//  from("build/webapp-ep2/") {
//    into("public")
//  }
//}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
  (this.tasks.getByName(name) as C).configuration()
}
