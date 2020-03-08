plugins {
    id("java")
    id("idea")
    id("io.freefair.lombok") version "5.0.0-rc4"
}

dependencies {
    implementation("org.apache.zookeeper:zookeeper:3.5.7")
    implementation("org.apache.curator:curator-recipes:4.3.0")
    implementation("org.redisson:redisson:3.12.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.apache.curator:curator-test:4.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

configurations.all {
    exclude(group = "org.slf4j", module = "slf4j-log4j12")
    exclude(group = "log4j", module = "log4j")
}