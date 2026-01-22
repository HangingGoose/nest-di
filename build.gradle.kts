plugins {
    `java-library`
    `maven-publish`
}

group = "dev.hanginggoose.nestdi"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api("org.reflections:reflections:0.10.2")
    api("org.jgrapht:jgrapht-core:1.5.2")
    api("org.javassist:javassist:3.30.2-GA")
    api("org.slf4j:slf4j-api:2.0.17")

    runtimeOnly("ch.qos.logback:logback-classic:1.5.23")

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Nest DI"
                description = "Lightweight, annotation-based Dependency-Injection framework for Java"
                url = "https://github.com/HangingGoose/nest-di"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://choosealicense.com/licenses/mit/"
                    }
                }
                developers {
                    developer {
                        id = "hanginggoose"
                        name = "HangingGoose"
                        email = "hanginggoose@outlook.com"
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/HangingGoose/nest-di.git")
                    developerConnection.set("scm:git:ssh://github.com/HangingGoose/nest-di.git")
                    url.set("https://github.com/HangingGoose/nest-di#")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/HangingGoose/nest-di")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")            }
        }
    }
}