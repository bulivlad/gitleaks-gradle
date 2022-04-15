package io.dotinc.gitleaks.gradle.task

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.RunEnvironment
import io.dotinc.gitleaks.gradle.scanner.Scanner


/**
 * @author vladbulimac on 14.04.2022.
 */

class Protect extends AbstractTask {

    Protect() {
        group = "GitLeaks scanner"
        description = "Protect secrets in already committed code"
    }

    protected void performScan(RunEnvironment environment) {
        Scanner.scan(environment, Scanner.Command.PROTECT, settings)
    }
}
