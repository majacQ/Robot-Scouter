package com.supercilex.robotscouter.build

import child
import com.supercilex.robotscouter.build.tasks.DeployServer
import com.supercilex.robotscouter.build.tasks.GenerateChangelog
import com.supercilex.robotscouter.build.tasks.RebuildSecrets
import com.supercilex.robotscouter.build.tasks.Setup
import com.supercilex.robotscouter.build.tasks.UpdateTranslations
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

internal class RobotScouterBuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project === project.rootProject) { "Cannot apply build plugin to subprojects." }

        project.tasks.register<Setup>("setup") {
            group = "build setup"
            description = "Performs one-time setup to prepare Robot Scouter for building."
        }
        project.tasks.register<RebuildSecrets>("rebuildSecrets") {
            group = "build setup"
            description = "Repackages a new version of the secrets for CI."
        }
        project.tasks.register<UpdateTranslations>("updateTranslations") {
            group = "build setup"
            description = "Overwrites existing translations with new ones."

            translationDir.set(project.file("tmp-translations"))
        }

        applyAndroidBase(project.child("android-base"))
        applyFunctions(project.child("functions"))
    }

    private fun applyAndroidBase(project: Project) {
        project.tasks.register<GenerateChangelog>("generateChangelog")
    }

    private fun applyFunctions(project: Project) {
        project.tasks.register<DeployServer>("deployServer") {
            group = "publishing"
            description = "Deploys Robot Scouter to the Web and Backend."

            dependsOn("assemble")
        }
    }
}
