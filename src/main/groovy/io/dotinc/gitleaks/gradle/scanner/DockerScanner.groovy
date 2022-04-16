package io.dotinc.gitleaks.gradle.scanner

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.Format
import io.dotinc.gitleaks.gradle.util.Settings
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.CONFIG_FILE
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.FORMAT
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.MASK_SENSITIVE_DATA
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.OUTPUT_DIRECTORY
import static io.dotinc.gitleaks.gradle.util.Settings.KEYS.SOURCE_PATH

/**
 * @author vladbulimac on 14.04.2022.
 */

final class DockerScanner extends Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerScanner.class);

    protected DockerScanner() {
    }

    protected void doScan(Command command, Settings settings) {
        this.createReportFolder(settings)
        def dockerCommand = this.buildDockerCommand(command, settings)
        def sout = new StringBuilder()
        def serr = new StringBuilder()

        LOGGER.debug("Executing command ${dockerCommand}")
        def proc = ["/bin/bash", "-c", dockerCommand].execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000 * 60)
        def exitCode = proc.exitValue()
        if (exitCode == 0) {
            LOGGER.info("No leaks found your project!")
            LOGGER.info(sout.toString())
            return
        }
        if (exitCode == 20) {
            LOGGER.info("Leaks found in your project. See the report for details!")
            LOGGER.info(serr.toString())
            return
        }
        if (serr.length() > 0) {
            LOGGER.error("Running docker command ${dockerCommand} failed with error ${serr.toString()}")
            throw new GroovyRuntimeException(serr.toString())
        }
        LOGGER.info(sout.toString())
    }

    private String buildDockerCommand(Command command, Settings settings) {
        def sb = new StringBuilder()
        sb.append("docker run")
        sb.append(" ")
        sb.append("-v ${settings.getString(SOURCE_PATH)}:/path")
        sb.append(" ")
        sb.append("zricethezav/gitleaks:latest")
        sb.append(" ")
        sb.append(command.command)
        sb.append(" ")
        sb.append("--source=\"/path\"")
        sb.append(" ")
        sb.append("-r")
        sb.append(" ")

        def format = Format.valueOf(settings.getString(FORMAT))
        if (format == Format.HTML) {
            format = Format.JSON
        }

        sb.append("/path/${settings.getString(OUTPUT_DIRECTORY)}/git-leaks-report${format.fileExtension}")
        sb.append(" ")
        sb.append("-f ${format}")

        def configFile = settings.getString(CONFIG_FILE)
        if (StringUtils.isNotBlank(configFile)) {
            sb.append(" ")
            sb.append("-c /path/${configFile}")
        }

        def maskSensitiveData = settings.getBoolean(MASK_SENSITIVE_DATA)
        if (maskSensitiveData) {
            sb.append(" ")
            sb.append("--redact")
        }

        sb.append(" ")
        sb.append("--exit-code=20")

        return sb.toString()
    }
}
