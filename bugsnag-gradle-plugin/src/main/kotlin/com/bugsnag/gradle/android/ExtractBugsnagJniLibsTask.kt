package com.bugsnag.gradle.android

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

open class ExtractBugsnagJniLibsTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout,
    private val fsOperations: FileSystemOperations,
    private val archiveOperations: ArchiveOperations
) : DefaultTask() {
    init {
        description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
    }

    @get:OutputDirectory
    val buildDirDestination: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(JNI_LIBS_DIR))

    @get:InputFiles
    val bugsnagArtifacts: ConfigurableFileCollection = objects.fileCollection()

    fun copy(action: (CopySpec) -> Unit): WorkResult = fsOperations.copy(action)
    fun zipTree(file: File): FileTree = archiveOperations.zipTree(file)

    /**
     * Looks at all the dependencies and their dependencies and finds the `com.bugsnag` artifacts with SO files.
     */
    @TaskAction
    fun setupNdkProject() {
        val destination = buildDirDestination.asFile.get()
        bugsnagArtifacts.forEach { file: File ->
            copy {
                it.from(zipTree(file))
                it.into(destination)
            }
        }
    }

    companion object {
        /**
         * Directory where SO files are extracted from bugsnag-android AARs
         */
        private const val JNI_LIBS_DIR = "intermediates/bugsnag-libs"

        private val sharedObjectAarIds = listOf(
            "bugsnag-android",
            "bugsnag-android-ndk",
            "bugsnag-plugin-android-anr",
            "bugsnag-plugin-android-ndk"
        )

        internal fun resolveBugsnagArtifacts(project: Project): FileCollection {
            val files = project.configurations
                .filter { it.toString().contains("CompileClasspath") }
                .map { it.resolvedConfiguration }
                .flatMap { it.firstLevelModuleDependencies }
                .filter { it.moduleGroup == "com.bugsnag" }
                .flatMap { it.allModuleArtifacts }
                .filter {
                    val identifier = it.id.componentIdentifier.toString()
                    sharedObjectAarIds.any { bugsnagId -> identifier.contains(bugsnagId) }
                }
                .map { it.file }
                .toSet()
            return project.files(files)
        }
    }
}
