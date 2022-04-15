package io.dotinc.gitleaks.gradle.scanner

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension
import io.dotinc.gitleaks.gradle.util.Settings
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @author vladbulimac on 15.04.2022.
 */

class NativeScanner extends Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeScanner.class);

    protected NativeScanner() {

    }

    protected void doScan(Command command, Settings settings) {
        this.createReportFolder(settings)
        def nativeCommand = this.buildNativeCommand(command, settings)
        def sout = new StringBuilder()
        def serr = new StringBuilder()

        LOGGER.debug("Executing command ${nativeCommand}")
        def proc = ["/bin/bash", "-c", nativeCommand].execute()
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
            LOGGER.error("Running docker command ${nativeCommand} failed with error ${serr.toString()}")
            throw new GroovyRuntimeException(serr.toString())
        }
        LOGGER.info(sout.toString())
    }

    private String buildNativeCommand(Command command, Settings settings) {
        def sb = new StringBuilder()
        sb.append("gitleaks")
        sb.append(" ")
        sb.append(command.command)
        sb.append(" ")
        sb.append("--source=\"${settings.getString(Settings.KEYS.SOURCE_PATH)}\"")
        sb.append(" ")
        sb.append("-r")
        sb.append(" ")

        def format = GitLeaksExtension.Format.valueOf(settings.getString(Settings.KEYS.FORMAT))

        sb.append("${settings.getString(Settings.KEYS.SOURCE_PATH)}/${settings.getString(Settings.KEYS.OUTPUT_DIRECTORY)}/git-leaks-report${format.fileExtension}")
        sb.append(" ")
        sb.append("-f ${format}")

        def configFile = settings.getString(Settings.KEYS.CONFIG_FILE)
        if (StringUtils.isNotBlank(configFile)) {
            sb.append(" ")
            sb.append("-c ${settings.getString(Settings.KEYS.SOURCE_PATH)}/${configFile}")
        }

        sb.append(" ")
        sb.append("--exit-code=20")

        return sb.toString()
    }
}
