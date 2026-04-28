plugins {
    id("java")
    id("org.springframework.boot")
}

group = "deusto"
version = "0.0.1-SNAPSHOT"

// ── Source set for the integration ──────────────────────────────────────────
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
    create("performanceTest") {
        java.srcDir("src/performanceTest/java")
        resources.srcDir("src/performanceTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

configurations {
    getByName("integrationTestImplementation").extendsFrom(configurations["testImplementation"])
    getByName("integrationTestRuntimeOnly").extendsFrom(configurations["testRuntimeOnly"])
    getByName("performanceTestImplementation").extendsFrom(configurations["testImplementation"])
    getByName("performanceTestRuntimeOnly").extendsFrom(configurations["testRuntimeOnly"])

}

// ── Dependencies ───────────────────────────────────────────────────────
dependencies {
    implementation(project(":lib"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("com.h2database:h2")

    // Testcontainers only for integration
    "integrationTestImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestImplementation"("org.testcontainers:mysql")
}

// ── Integration Task ───────────────────────────────────────────────
tasks.register<Test>("integrationTest") {
    description = "Ejecuta los tests de integración"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()

    // Evita que docker-compose del servidor interfiera al testear
    systemProperty("spring.docker.compose.enabled", "false")
    systemProperty("api.version", "1.41")
}
tasks.register<Test>("performanceTest") {
    description = "Ejecuta los tests de rendimiento"
    group = "verification"
    testClassesDirs = sourceSets["performanceTest"].output.classesDirs
    classpath = sourceSets["performanceTest"].runtimeClasspath
    useJUnitPlatform()
}

// build will not launch the tests
tasks.build {
    setDependsOn(dependsOn.filter { it.toString() != "integrationTest" && it.toString() != "performanceTest"  })
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
}