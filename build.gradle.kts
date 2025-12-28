plugins {
    java
    id("application")
}

group = "dev.hanginggoose.nestdi"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "dev.hanginggoose.nestdi.demo.DemoApplication"
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")

    implementation("org.javassist:javassist:3.30.2-GA")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.23")

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

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "dev.hanginggoose.nestdi.demo.DemoApplication",
            "Implementation-Title" to "Nest DI",
            "Implementation-Version" to version
        )
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}