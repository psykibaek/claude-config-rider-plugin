package com.gear2w.claudeconfig

import com.intellij.openapi.project.Project
import java.io.File
import javax.swing.tree.DefaultMutableTreeNode

// 프로젝트 / 글로벌의 Claude 설정 파일을 스캔해 트리 모델용 루트 노드를 만든다.
object ClaudeConfigScanner {

    fun buildRoot(project: Project): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(ConfigNode("Claude Config"))

        // 1) 프로젝트 영역
        project.basePath?.let { basePath ->
            val base = File(basePath)
            val projectNode = DefaultMutableTreeNode(ConfigNode("프로젝트: ${project.name}"))

            // 루트의 CLAUDE.md
            addFileIfExists(projectNode, File(base, "CLAUDE.md"))
            // .claude 디렉터리 하위
            addClaudeDir(projectNode, File(base, ".claude"))

            if (projectNode.childCount > 0) root.add(projectNode)
            else root.add(emptyHint("프로젝트에 Claude 설정 파일이 없습니다"))
        }

        // 2) 글로벌 영역 (~/.claude)
        val home = File(System.getProperty("user.home"))
        val globalClaude = File(home, ".claude")
        val globalNode = DefaultMutableTreeNode(ConfigNode("글로벌: ~/.claude"))
        addClaudeDir(globalNode, globalClaude)
        if (globalNode.childCount > 0) root.add(globalNode)

        return root
    }

    // .claude 디렉터리 하위의 CLAUDE.md 와 skills 폴더 트리를 노드로 추가한다.
    private fun addClaudeDir(parent: DefaultMutableTreeNode, claudeDir: File) {
        if (!claudeDir.isDirectory) return

        addFileIfExists(parent, File(claudeDir, "CLAUDE.md"))

        val skillsDir = File(claudeDir, "skills")
        if (skillsDir.isDirectory) {
            val skillsNode = DefaultMutableTreeNode(ConfigNode("skills"))
            val entries = skillsDir.listFiles()?.sortedBy { it.name } ?: emptyList()
            for (entry in entries) {
                when {
                    entry.isDirectory -> {
                        // 스킬 폴더: 내부의 .md 파일들을 나열
                        val skillNode = DefaultMutableTreeNode(ConfigNode(entry.name))
                        val mds = entry.listFiles { f -> f.isFile && f.extension == "md" }
                            ?.sortedBy { it.name } ?: emptyList()
                        for (md in mds) {
                            skillNode.add(DefaultMutableTreeNode(ConfigNode(md.name, md)))
                        }
                        if (skillNode.childCount > 0) skillsNode.add(skillNode)
                    }
                    entry.isFile && entry.extension == "md" -> {
                        skillsNode.add(DefaultMutableTreeNode(ConfigNode(entry.name, entry)))
                    }
                }
            }
            if (skillsNode.childCount > 0) parent.add(skillsNode)
        }
    }

    private fun addFileIfExists(parent: DefaultMutableTreeNode, file: File) {
        if (file.isFile) {
            parent.add(DefaultMutableTreeNode(ConfigNode(file.name, file)))
        }
    }

    private fun emptyHint(text: String) = DefaultMutableTreeNode(ConfigNode(text))
}
