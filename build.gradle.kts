plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "samwells.io"
version = "0.0.1-SNAPSHOT"
description = "Multipart uploading w/ S3"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // API
	implementation("org.springframework.boot:spring-boot-starter-web")

    // S3
    implementation("software.amazon.awssdk:s3:2.34.4")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.34.4")

    // AWS SSO
    implementation("software.amazon.awssdk:sso:2.34.4")
    implementation("software.amazon.awssdk:ssooidc:2.34.4")

    // Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

    // Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
