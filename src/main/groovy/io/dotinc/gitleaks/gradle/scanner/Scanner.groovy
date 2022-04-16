package io.dotinc.gitleaks.gradle.scanner

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.Format
import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.RunEnvironment
import io.dotinc.gitleaks.gradle.util.Settings


/**
 * @author vladbulimac on 14.04.2022.
 */

abstract class Scanner {
    abstract protected void doScan(Command command, Settings settings)

    static scan(RunEnvironment environment, Command command, Settings settings) {
        def scanner = ScanStrategy.getScanner(environment)
        scanner.doScan(command, settings)
    }

    static getReportFile(String outputLocation, Format format) {
        def outFile = new File(outputLocation)
        if (outFile.getParentFile() == null) {
            outFile = new File(".", outputLocation)
        }

        def pathToCheck = outputLocation.toLowerCase()
        if (format == Format.JSON && !pathToCheck.endsWith(Format.JSON.fileExtension)) {
            return new File(outFile, "git-leaks-report.json")
        }
        if (format == Format.CSV && !pathToCheck.endsWith(Format.CSV.fileExtension)) {
            return new File(outFile, "git-leaks-report.csv")
        }
        if (format == Format.SARIF && !pathToCheck.endsWith(Format.SARIF.fileExtension)) {
            return new File(outFile, "git-leaks-report.sarif")
        }
        if (format == Format.HTML && !pathToCheck.endsWith(Format.HTML.fileExtension)) {
            return new File(outFile, "git-leaks-report.html")
        }

        return outFile
    }

    protected void createReportFolder(Settings settings) {
        def output = new File(settings.getString(Settings.KEYS.OUTPUT_DIRECTORY))
        if (!output.exists()) {
            output.mkdir()
        }
    }

    enum Command {
        DETECT("detect"),
        PROTECT("protect")

        String command

        Command(String command) {
            this.command = command
        }
    }
}
