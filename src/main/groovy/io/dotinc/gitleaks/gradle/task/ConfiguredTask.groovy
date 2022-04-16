package io.dotinc.gitleaks.gradle.task

import io.dotinc.gitleaks.gradle.service.ReportExportService
import io.dotinc.gitleaks.gradle.service.VaultService
import io.dotinc.gitleaks.gradle.util.Settings
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.CONFIG_FILE
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.MASK_SENSITIVE_DATA
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.SKIP
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.FAIL_ON_ERROR
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.FORMAT
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.OUTPUT_DIRECTORY
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.RUN_ENVIRONMENT
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.SOURCE_PATH


/**
 * @author vladbulimac on 14.04.2022.
 */

abstract class ConfiguredTask extends DefaultTask {

    @Internal
    def config = project.gitLeaks
    @Internal
    def vault = config.vault
    @Internal
    def export = config.export
    @Internal
    def settings

    protected void initializeSettings() {
        settings = new Settings()

        settings.setBooleanIfNotNull(SKIP, config.skip)
        settings.setBooleanIfNotNull(FAIL_ON_ERROR, config.failOnError)
        settings.setStringIfNotEmpty(CONFIG_FILE, config.configFile)
        settings.setStringIfNotEmpty(SOURCE_PATH, getGitRootDir())
        settings.setStringIfNotEmpty(FORMAT, config.format.toString())
        settings.setStringIfNotEmpty(OUTPUT_DIRECTORY, config.outputDirectory)
        settings.setStringIfNotEmpty(RUN_ENVIRONMENT, config.runEnvironment.toString())
        settings.setBooleanIfNotNull(MASK_SENSITIVE_DATA, config.maskSensitiveData)

        configureVault(settings)
        configureExport(settings)
    }

    private void configureVault(Settings settings) {
        settings.setStringIfNotEmpty(VaultService.VAULT__FILE__PATH, vault.vaultPath)
        settings.setStringIfNotEmpty(VaultService.VAULT__KEYFILE, vault.vaultKeyFile)
    }

    private void configureExport(Settings settings) {
        settings.setBooleanIfNotNull(ReportExportService.EXPORT__VAULT__LOOKUP, export.vaultLookup)
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__URL, export.url)
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__AUTH__TYPE, export.authType.toString())
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__BEARER__TOKEN, export.bearerToken)
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__BASIC__TOKEN, export.basicToken)
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__BASIC__USERNAME, export.basicUsername)
        settings.setStringIfNotEmpty(ReportExportService.EXPORT__BASIC__PASSWORD, export.basicPassword)
    }

    private String getGitRootDir() {
        def sout = new StringBuilder()
        def serr = new StringBuilder()

        def gitCommand = "git rev-parse --show-toplevel"
        logger.debug("Executing command ${gitCommand}")
        def proc = ["/bin/bash", "-c", gitCommand].execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000 * 60)
        if (serr.length() > 0) {
            logger.error("No git dir found in path!")
            return null
        }
        return sout.toString().trim()
    }
}
