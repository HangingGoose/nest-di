plugins {
    id("java")
}

group = "dev.hanginggoose"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
}

tasks.test {
    useJUnitPlatform()
}