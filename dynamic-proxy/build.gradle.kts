plugins {
    id("java")
    id("idea")
    id("io.freefair.lombok") version "5.0.0-rc4"
}

dependencies {
    implementation("cglib:cglib:3.3.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
//    compileOnly("org.projectlombok:lombok:1.18.12")
//    annotationProcessor("org.projectlombok:lombok:1.18.12")
//
//    testCompileOnly("org.projectlombok:lombok:1.18.12")
//    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}