package com.gear2w.claudeconfig

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * CLAUDE.md 의 `@경로` import 가 실제로 존재하는지 검증한다.
 * 존재하지 않으면 해당 경로에 빨간 물결 밑줄(미해결 참조 스타일)을 표시한다.
 */
class ClaudeImportAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // 파일당 한 번만 전체 텍스트를 스캔한다.
        if (element !is PsiFile) return
        val vFile = element.virtualFile ?: return
        if (!ClaudePaths.isMemoryFile(vFile)) return
        val fileDir = vFile.parent?.path ?: return

        val text = element.text
        for (match in IMPORT.findAll(text)) {
            val group = match.groups[1] ?: continue
            // 문장 끝 구두점은 경로에서 제외
            val path = group.value.trimEnd('.', ',', ';', ':', ')', '`')
            if (path.isEmpty()) continue

            val target = ClaudePaths.resolve(path, fileDir)
            if (!target.exists()) {
                val start = group.range.first
                val range = TextRange(start, start + path.length)
                holder.newAnnotation(HighlightSeverity.WARNING, "Imported path not found: $path")
                    .range(range)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create()
            }
        }
    }

    private companion object {
        // 줄 시작 또는 공백 뒤의 @경로 (경로는 공백이 아닌 연속 문자)
        private val IMPORT = Regex("""(?<=^|\s)@(\S+)""", RegexOption.MULTILINE)
    }
}
