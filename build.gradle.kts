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
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
                name.set("Nest DI")
                description.set("Lightweight, annotation-based Dependency-Injection framework for Java")
                url.set("https://github.com/HangingGoose/nest-di")

                licenses {
                    license {
                        name.set("MIT")
                    }
                }

                developers {
                    developer {
                        id.set("hanginggoose")
                        name.set("HangingGoose")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/HangingGoose/nest-di.git")
                    developerConnection.set("scm:git:ssh://github.com/HangingGoose/nest-di.git")
                    url.set("https://github.com/HangingGoose/nest-di")
                }
            }
        }
    }

    repositories {
        mavenLocal()

        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: ""
                password = project.findProperty("ossrhPassword") as String? ?: ""
            }
        }

        // Alternative: GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/yourusername/nest-di")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Nest DI",
            "Implementation-Version" to version
        )
    }
}