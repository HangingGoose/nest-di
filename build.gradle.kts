plugins {
    java
    id("application")
}

group = "dev.hanginggoose"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "dev.hanginggoose.demo.DemoApplication"
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")

    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}