package com.anaphan.hiddengitignore

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path

class OpenLocalGitignoreAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        openGitExclude(project)
    }

    private fun openGitExclude(project: Project) {
        val projectRoot = project.basePath?.let(Path::of)

        if (projectRoot == null) {
            notifyError(project)
            return
        }

        val excludeFile = projectRoot
            .resolve(".git")
            .resolve("info")
            .resolve("exclude")

        if (!excludeFile.toFile().isFile) {
            notifyError(project)
            return
        }

        val virtualFile = LocalFileSystem
            .getInstance()
            .refreshAndFindFileByNioFile(excludeFile)

        if (virtualFile == null) {
            notifyError(project)
            return
        }

        FileEditorManager
            .getInstance(project)
            .openFile(virtualFile, true)
    }

    private fun notifyError(project: Project) {
        val notification = Notification(
            "HiddenGitignore.Group",
            "Unable to open local gitignore",
            "Could not find <code>.git/info/exclude</code>. " +
                    "Make sure this project has a local Git repository.",
            NotificationType.ERROR
        )

        Notifications.Bus.notify(notification, project)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
