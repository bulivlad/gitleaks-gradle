package io.dotinc.gitleaks.gradle.task

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.RunEnvironment
import io.dotinc.gitleaks.gradle.scanner.Scanner

/**
 * @author vladbulimac on 14.04.2022.
 */

class Detect extends AbstractTask {

    Detect() {
        group = "GitLeaks scanner"
        description = "Detect secrets in uncommitted code"
    }

    protected void performScan(RunEnvironment environment) {
        Scanner.scan(environment, Scanner.Command.DETECT, settings)
    }
}
