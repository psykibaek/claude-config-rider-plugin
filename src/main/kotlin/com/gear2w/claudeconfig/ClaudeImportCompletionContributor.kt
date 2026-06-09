package com.gear2w.claudeconfig

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import java.io.File

/**
 * CLAUDE.md 의 `@경로` import 문법에 대한 파일 경로 자동완성.
 *
 * - `@` 를 입력하면 현재 파일 기준 디렉터리의 항목이 팝업으로 뜬다.
 * - `~/` 로 시작하면 홈 디렉터리, `/` 로 시작하면 절대 경로 기준으로 해석한다.
 * - 폴더를 선택하면 `/` 가 붙고 곧바로 하위 자동완성이 다시 열린다.
 */
class ClaudeImportCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), ImportPathProvider())
    }

    // '@' 를 타이핑하는 즉시 자동완성 팝업이 뜨도록 한다.
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean = typeChar == '@'

    private class ImportPathProvider : CompletionProvider<CompletionParameters>() {

        // 경로 목록에서 숨길 잡음 디렉터리/파일
        private val noise = setOf(
            ".git", ".idea", ".gradle", ".vs", ".DS_Store",
            "node_modules", "bin", "obj", "build", "out",
        )

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val vFile = parameters.originalFile.virtualFile ?: return
            if (!isClaudeMemoryFile(vFile)) return

            // dummy identifier 가 끼지 않은 실제 문서/오프셋에서 '@' 토큰을 직접 파싱한다.
            val document = parameters.editor.document
            val caret = parameters.offset
            val lineStart = document.getLineStartOffset(document.getLineNumber(caret))
            val before = document.getText(TextRange(lineStart, caret))

            val at = before.lastIndexOf('@')
            if (at < 0) return
            // '@' 앞은 줄 시작이거나 공백이어야 한다(이메일 등 오인 방지).
            if (at > 0 && !before[at - 1].isWhitespace()) return

            val token = before.substring(at + 1)
            if (token.any { it.isWhitespace() }) return

            val dirPrefix = if (token.contains('/')) token.substringBeforeLast('/') else ""
            val namePart = token.substringAfterLast('/')

            val fileDir = vFile.parent?.path ?: return
            val home = System.getProperty("user.home")
            val baseDir = when {
                dirPrefix.startsWith("~") -> File(home, dirPrefix.removePrefix("~").removePrefix("/"))
                dirPrefix.startsWith("/") -> File(dirPrefix)
                dirPrefix.isEmpty() -> File(fileDir)
                else -> File(fileDir, dirPrefix)
            }

            val children = baseDir.listFiles()
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?: return

            // 사용자가 점(.)으로 시작해 입력하면 숨김/점 파일도 보여준다.
            val showHidden = namePart.startsWith(".")
            val matched = result.withPrefixMatcher(namePart)

            for (child in children) {
                if (child.name in noise) continue
                if (child.name.startsWith(".") && !showHidden) continue

                val isDir = child.isDirectory
                val insert = if (isDir) child.name + "/" else child.name

                var element = LookupElementBuilder.create(insert)
                    .withIcon(if (isDir) AllIcons.Nodes.Folder else AllIcons.FileTypes.Text)
                    .withTypeText(if (isDir) "dir" else "file", true)

                if (isDir) {
                    // 폴더 선택 시 하위 자동완성을 곧바로 다시 연다.
                    element = element.withInsertHandler { ctx, _ ->
                        AutoPopupController.getInstance(ctx.project).scheduleAutoPopup(ctx.editor)
                    }
                }
                matched.addElement(element)
            }
        }

        private fun isClaudeMemoryFile(vFile: VirtualFile): Boolean {
            if (vFile.name == "CLAUDE.md") return true
            if (vFile.extension?.lowercase() != "md") return false
            // .claude 디렉터리 하위의 .md 파일도 대상으로 포함
            var dir: VirtualFile? = vFile.parent
            while (dir != null) {
                if (dir.name == ".claude") return true
                dir = dir.parent
            }
            return false
        }
    }
}
