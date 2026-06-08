package com.gear2w.claudeconfig

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * 툴윈도우 본문. 상단에 새로고침 툴바, 본문에 설정 파일 트리를 표시한다.
 */
class ClaudeConfigPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val tree = Tree()

    init {
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.isRootVisible = false
        tree.showsRootHandles = true

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) openSelectedFile()
            }
        })

        add(createToolbar().component, BorderLayout.NORTH)
        add(JBScrollPane(tree), BorderLayout.CENTER)

        reload()
    }

    private fun createToolbar(): ActionToolbar {
        val group = DefaultActionGroup()
        group.add(object : AnAction("Refresh", "Claude 설정 파일을 다시 스캔합니다", AllIcons.Actions.Refresh), DumbAware {
            override fun actionPerformed(e: AnActionEvent) = reload()
        })
        group.add(object : AnAction("Open", "선택한 파일을 에디터에서 엽니다", AllIcons.Actions.MenuOpen), DumbAware {
            override fun actionPerformed(e: AnActionEvent) = openSelectedFile()
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("ClaudeConfigToolbar", group, true)
        toolbar.targetComponent = this
        return toolbar
    }

    /** 트리를 다시 만들고 모든 노드를 펼친다. */
    fun reload() {
        val root = ClaudeConfigScanner.buildRoot(project)
        tree.model = DefaultTreeModel(root)
        var row = 0
        while (row < tree.rowCount) {
            tree.expandRow(row)
            row++
        }
    }

    private fun openSelectedFile() {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
        val configNode = node.userObject as? ConfigNode ?: return
        val file = configNode.file ?: return

        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: return
        FileEditorManager.getInstance(project).openFile(vFile, true)
    }
}
