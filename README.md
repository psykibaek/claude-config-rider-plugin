# Claude Config Manager (Rider Plugin)

Rider(및 모든 IntelliJ 계열 IDE)에서 프로젝트/글로벌에 흩어져 있는 **Claude Code 설정 파일을 한 곳에서 모아 보고 편집**하는 플러그인입니다.

오른쪽 **Claude Config** 툴윈도우에 다음을 트리로 표시합니다.

- 프로젝트 루트의 `CLAUDE.md`
- 프로젝트 `.claude/` 의 `CLAUDE.md`, `skills/**`
- 글로벌 `~/.claude/` 의 `CLAUDE.md`, `skills/**`

파일을 **더블클릭**하면 에디터에서 열려 바로 수정할 수 있습니다. 상단 툴바의 **Refresh** 로 다시 스캔합니다.

또한 CLAUDE.md 를 편집할 때 **`@경로` import 자동완성**을 제공합니다. `@` 를 입력하면 현재 파일
기준 디렉터리의 파일/폴더가 팝업으로 뜨고, `~/`(홈) · `/`(절대경로) 도 인식하며, 폴더를 선택하면
하위 자동완성이 이어집니다. 존재하지 않는 경로는 **빨간 물결 밑줄**(미해결 참조 스타일)로 표시됩니다.

---

## 1. 요구 사항

- JDK 21 은 **자동으로** 내려받습니다(`foojay-resolver`). 로컬에 없어도 됩니다. Gradle Wrapper 실행에는 JDK 17+ 가 필요합니다.
- 최초 빌드 시 Gradle 이 **Rider 배포본(약 1.5GB+)** 을 내려받으므로 시간이 걸립니다.

## 2. 로컬 실행 / 디버그

샌드박스 Rider 를 띄워 플러그인을 바로 테스트합니다.

```bash
./gradlew runIde
```

## 3. 설치용 zip 빌드

```bash
./gradlew buildPlugin
# 결과물: build/distributions/claude-config-manager-0.1.0.zip
```

Rider 에서 설치:
`Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk...` → 위 zip 선택 → 재시작.

## 4. 마켓플레이스 배포

### 4-1. 벤더 계정 / 토큰

1. https://plugins.jetbrains.com 에 JetBrains 계정으로 로그인 후 **벤더(Vendor)** 생성.
2. `My Account → Personal Access Tokens` 에서 **Marketplace 업로드 토큰** 발급.

### 4-2. (권장) 플러그인 서명 키 준비

```bash
# 개인키 생성
openssl genpkey -aes-256-cbc -algorithm RSA -out private_encrypted.pem -pkeyopt rsa_keygen_bits:4096
# 인증서 체인 생성
openssl req -key private_encrypted.pem -new -x509 -days 3650 -out chain.crt
```

### 4-3. 환경변수 설정 후 publish

`build.gradle.kts` 는 아래 환경변수를 읽습니다.

```bash
export PUBLISH_TOKEN="...marketplace token..."
# 서명을 사용할 경우(선택)
export CERTIFICATE_CHAIN="$(cat chain.crt)"
export PRIVATE_KEY="$(cat private_encrypted.pem)"
export PRIVATE_KEY_PASSWORD="개인키 암호"

# 호환성 검증(선택, 권장)
./gradlew verifyPlugin

# 업로드
./gradlew publishPlugin
```

> 최초 업로드는 마켓플레이스 심사를 거칩니다. 심사 후에는 `publishPlugin` 으로 새 버전이 바로 반영됩니다.
> 토큰/서명 없이 단순히 zip 만 수동 업로드하려면 `buildPlugin` 결과 zip 을 마켓플레이스 웹에서 올려도 됩니다.

## 5. 버전 올리기

`gradle.properties` 의 `pluginVersion` 을 수정한 뒤 다시 빌드/배포합니다.

## 6. 구조

```
src/main/kotlin/com/gear2w/claudeconfig/
  ClaudeConfigToolWindowFactory.kt  # 툴윈도우 등록
  ClaudeConfigPanel.kt              # 툴바 + 트리 UI, 더블클릭 시 파일 열기
  ClaudeConfigScanner.kt            # 프로젝트/글로벌 설정 파일 스캔
  ConfigNode.kt                     # 트리 노드 모델
src/main/resources/
  META-INF/plugin.xml               # 플러그인 디스크립터
  icons/claude.svg                  # 툴윈도우 아이콘
```
