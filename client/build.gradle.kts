plugins {
    id ("java")
    id("org.springframework.boot")
}

group = "deusto"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":lib"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = false
}