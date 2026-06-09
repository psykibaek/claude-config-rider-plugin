plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // 빌드 대상: Rider
        rider(providers.gradleProperty("platformVersion"))

        // 마켓플레이스 배포 전 호환성 검증 / zip 서명 도구
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    // 순수 Kotlin 플러그인이라 Java 코드 계측이 불필요.
    // Rider 타깃에서는 java-compiler-ant-tasks 가 해석되지 않으므로 비활성화한다.
    instrumentCode = false

    pluginConfiguration {
        id = "com.gear2w.claude-config"
        name = "Claude Config Manager"
        version = providers.gradleProperty("pluginVersion")

        description =
            """
            Browse and edit all your Claude Code configuration files in one place, directly from
            Rider or any IntelliJ-based IDE. The plugin adds a "Claude Config" tool window that
            collects the configuration files scattered across your project and home directory:
            <ul>
              <li>The project root <code>CLAUDE.md</code></li>
              <li>The project <code>.claude/</code> directory: its <code>CLAUDE.md</code> and <code>skills</code></li>
              <li>The global <code>~/.claude/</code> directory: its <code>CLAUDE.md</code> and <code>skills</code></li>
            </ul>
            Double-click any file in the tree to open it in the editor and edit it right away.
            Use the Refresh action to rescan after files are added or removed.
            """.trimIndent()

        changeNotes =
            """
            0.2.0: Added path autocompletion for the <code>@import</code> syntax inside CLAUDE.md
            (type <code>@</code> to complete files and folders, including <code>~/</code> and absolute paths).<br/>
            0.1.0: Initial release: a tool window to browse and edit Claude Code configuration files.
            """.trimIndent()

        ideaVersion {
            sinceBuild = "243"
            // 상한을 두지 않아 이후 Rider 버전과도 호환되도록 함
            untilBuild = provider { null }
        }

        vendor {
            name = "gear2w"
            email = "devyou@gear2w.com"
        }
    }

    // zip 서명 (마켓플레이스 권장). 환경변수가 없으면 서명 단계는 건너뜀.
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    // 마켓플레이스 배포 토큰
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

kotlin {
    jvmToolchain(21)
}
