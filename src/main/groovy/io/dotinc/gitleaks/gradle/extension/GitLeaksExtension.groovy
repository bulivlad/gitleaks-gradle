package io.dotinc.gitleaks.gradle.extension

import org.gradle.api.Project


/**
 * Configuration extension for the gitleaks plugin.
 *
 * @author vladbulimac on 14.04.2022.
 */

class GitLeaksExtension {

    GitLeaksExtension(Project project) {
        this.project = project
        this.outputDirectory = "${project.rootDir.relativePath(project.buildDir)}/gitleak-report"
    }

    Project project

    /**
     * The directory where the reports will be written relative to project root dir. Defaults to '${projectDir}/build/reports'.
     */
    String outputDirectory

    /**
     * Whether the scan should be skipped or not.
     */
    Boolean skip = false

    /**
     * Whether the plugin should fail when error occur.
     */
    Boolean failOnError = false

    /**
     * Relative path to the configuration file.
     */
    String configFile

    /**
     * Relative path to the folder to be scanned.
     */
    String relativeSourcePath = "${project.rootDir.relativePath(project.projectDir)}"

    /**
     * The report format to be generated (JSON, CSV, SARIF). This configuration option has
     * no effect if using this within the Site plugin unless the externalReport is set to true.
     * The default is JSON.
     */
    Format format = Format.JSON

    /**
     * The environment where the scan should run.
     * Default is DOCKER.
     */
    RunEnvironment runEnvironment = RunEnvironment.DOCKER

    /**
     * The configuration extensions for the vault.
     */
    VaultExtension vault = new VaultExtension();

    /**
     * The configuration extensions for the report export extension.
     */
    ReportExportExtension export = new ReportExportExtension();

    /**
     * Allows programmatic configuration of the vault extension
     * @param configClosure the closure to configure the vault extension
     * @return the vault extension
     */
    def vault(Closure configClosure) {
        return project.configure(vault, configClosure)
    }

    /**
     * Allows programmatic configuration of the report export extension
     * @param configClosure the closure to configure the report export extension
     * @return the report export extension
     */
    def export(Closure configClosure) {
        return project.configure(export, configClosure)
    }

    enum Format {
        JSON(".json"),
        CSV(".csv"),
        SARIF(".sarif"),
        HTML(".html")

        String fileExtension

        Format(String fileExtension) {
            this.fileExtension = fileExtension
        }
    }

    enum RunEnvironment {
        DOCKER,
        NATIVE
    }
}
