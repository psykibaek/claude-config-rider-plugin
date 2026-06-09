package com.gear2w.claudeconfig

import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * CLAUDE.md 의 @import 경로 해석/대상 파일 판별을 자동완성과 검증이 공유한다.
 */
object ClaudePaths {

    /** 자동완성/검증을 적용할 파일인지: CLAUDE.md 또는 .claude 디렉터리 하위의 .md */
    fun isMemoryFile(vFile: VirtualFile): Boolean {
        if (vFile.name == "CLAUDE.md") return true
        if (vFile.extension?.lowercase() != "md") return false
        var dir: VirtualFile? = vFile.parent
        while (dir != null) {
            if (dir.name == ".claude") return true
            dir = dir.parent
        }
        return false
    }

    /**
     * import 토큰(`@` 뒤의 경로)을 실제 파일로 해석한다.
     * - `~` 로 시작하면 홈 디렉터리 기준
     * - `/` 로 시작하면 절대 경로
     * - 그 외에는 현재 파일이 있는 디렉터리 기준 상대 경로
     */
    fun resolve(token: String, fileDir: String): File = when {
        token.startsWith("~") -> File(System.getProperty("user.home"), token.removePrefix("~").removePrefix("/"))
        token.startsWith("/") -> File(token)
        else -> File(fileDir, token)
    }
}
