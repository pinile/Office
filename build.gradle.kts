plugins {
    id("java")
}

group = "ru.stepchenkov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.assertj:assertj-core:3.26.3")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("io.rest-assured:rest-assured:5.5.6")
    implementation("io.rest-assured:json-schema-validator:5.5.6")

    implementation("org.slf4j:slf4j-api:2.0.16")

    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("ch.qos.logback:logback-core:1.5.19")

    implementation("org.jdbi:jdbi3-core:3.49.6")
    implementation("org.jdbi:jdbi3-sqlobject:3.49.6")

    implementation("org.aeonbits.owner:owner:1.0.12")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.h2database:h2:2.3.232")
}

tasks.test {
    useJUnitPlatform()
}