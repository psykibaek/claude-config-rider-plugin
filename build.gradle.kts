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
            Rider 프로젝트에 흩어져 있는 Claude Code 설정 파일을 한곳에서 모아 보고 편집합니다.
            <ul>
              <li>프로젝트 루트의 <code>CLAUDE.md</code></li>
              <li>프로젝트 <code>.claude/</code> 아래의 <code>CLAUDE.md</code> 와 <code>skills/**</code></li>
              <li>글로벌 <code>~/.claude/</code> 의 <code>CLAUDE.md</code> 와 <code>skills/**</code></li>
            </ul>
            툴윈도우 트리에서 파일을 더블클릭하면 에디터에서 바로 열어 수정할 수 있습니다.
            """.trimIndent()

        changeNotes = "최초 릴리스: Claude 설정 파일 모아보기 / 편집 툴윈도우."

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
