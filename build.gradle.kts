plugins {
    id("java")
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ru.avishgreen"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Используем Spring Boot Starter Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")


    implementation("org.telegram:telegrambots-springboot-longpolling-starter:9.0.0")
    implementation("org.telegram:telegrambots-client:9.0.0")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core:4.33.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    //mapstruct должен идти после lombok иначе он не видит созданные ломбок сеттеры
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    // Это необязательно, но очень полезно для связки lombok и mapstruct
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}