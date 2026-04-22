plugins {
    id ("java")
    id("org.springframework.boot")
}

group = "deusto"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":lib"))
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = false
}