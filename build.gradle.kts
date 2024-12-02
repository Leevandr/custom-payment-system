plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.spring") version "1.9.0" // Для поддержки Spring с Kotlin (если нужно)
}

group = "com.levandr"
version = "0.0.1-SNAPSHOT"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Разработка
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Базы данных
    runtimeOnly("com.h2database:h2") // Для in-memory базы данных H2
    runtimeOnly("org.postgresql:postgresql") // Для подключения к PostgreSQL

    // Логирование
    implementation("org.springframework.boot:spring-boot-starter-logging")

    // Lombok (удобство работы с геттерами/сеттерами, конструкторами)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Тестирование
    testImplementation("org.springframework.boot:spring-boot-starter-test") // Включает JUnit 5, Mockito и другие
    testImplementation("org.mockito:mockito-core:5.11.0") // Для мокирования
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3") // JUnit 5 API
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2") // JUnit 5 Engine
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // Для запуска JUnit тестов

    // Дополнительные зависимости для тестов (например, если нужны интеграционные тесты)
    testImplementation("org.springframework:spring-test:6.2.0") // Spring Test для тестирования
}

tasks.withType<Test> {
    useJUnitPlatform() // Для работы с JUnit 5
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
