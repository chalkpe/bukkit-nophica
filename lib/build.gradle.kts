plugins {
  kotlin("jvm") version "2.0.10"
  id("com.gradleup.shadow") version "8.3.0"
  id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "pe.chalk.bukkit"
version = "1.0.1"

repositories {
  mavenCentral()
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
  compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
  implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks.shadowJar {
  archiveClassifier.set("")
  relocate("org.bstats", "pe.chalk.bukkit.bstats")
}

bukkit {
  main = "pe.chalk.bukkit.nophica.Nophica"
  apiVersion = "1.20.6"
}