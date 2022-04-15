package io.dotinc.gitleaks.gradle.task

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.Format
import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.RunEnvironment
import io.dotinc.gitleaks.gradle.scanner.Scanner
import io.dotinc.gitleaks.gradle.service.ReportExportService
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction


/**
 * @author vladbulimac on 14.04.2022.
 */

abstract class AbstractTask extends ConfiguredTask {

    abstract protected void performScan(RunEnvironment environment)

    @TaskAction
    run() {
        if (config.skip) {
            logger.lifecycle("Skipping git-leaks scan.")
            return
        }
        initializeSettings()

        File output = new File(project.projectDir.toString() + "/" + config.outputDirectory.toString())

        try {
            if (config.outputDirectory.startsWith("/") ||
                    project.rootDir.toPath().relativize(output.toPath()).startsWith(".")) {
                logger.error("Report output path should be inside project path!")
                throw new GradleException("Report output path should be inside project path!")
            }
            performScan(config.runEnvironment)
            exportReport(output, config.format)
        } catch(Exception ex) {
            if (config.failOnError) {
                throw new GradleException("There was and error executing git-leaks plugin", ex)
            } else {
                logger.warn("There was an error executing git-leak plugin. No report was generated", ex)
            }
        }
    }

    void exportReport(File output, Format format) {
        if (null == export.url) {
            return
        }
        def outputPath = output.getAbsolutePath()
        def exportService = new ReportExportService(settings)
        def reportFile = Scanner.getReportFile(outputPath, format)
        exportService.export(reportFile, format)
    }


}
