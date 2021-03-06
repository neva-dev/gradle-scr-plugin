package com.neva.gradle.plugin.scr.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

public class AppendServicesToManifest extends DefaultTask {

    static final String TASK_NAME = 'appendServicesToManifest'

    static final String MANIFEST_ENTRY = "Service-Component"

    static final String OSGI_DIR = 'OSGI-INF'

    public PackageAction() {
        group = 'Build'
        description = "Iterates over xml files generated by ${ProcessSrcAnnotations.MAIN_TASK_NAME} and adds those services to MANIFEST.MF."
    }

    @TaskAction
    def run() {
        final osgiInfDir = new File(mainSourceSet(project).output.classesDir, OSGI_DIR)

        def files = osgiInfDir.listFiles({ File dir, String name ->
            name.endsWith(".xml")
        } as FilenameFilter) as List<File>

        if (files && !files.isEmpty()) {
            def relFiles = files.collect { file -> OSGI_DIR + '/' + file.name }
            def strFiles = relFiles.join(',')

            project.logger.info "Created service components: ${relFiles}"

            def bundleTask = project.tasks.findByName('bundle')
            if (bundleTask != null) {
                project.bundle.instruction(MANIFEST_ENTRY, strFiles)
            } else {
                project.jar.manifest.instruction(MANIFEST_ENTRY, strFiles)
            }
        }
    }

    def SourceSet mainSourceSet(Project project) {
        project.convention.findPlugin(JavaPluginConvention)?.sourceSets?.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    }
}