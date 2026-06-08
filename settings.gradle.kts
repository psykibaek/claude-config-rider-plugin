plugins {
    // JDK 21 toolchain 을 자동으로 내려받아 빌드에 사용 (로컬에 JDK 21 이 없어도 됨)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "claude-config-manager"
