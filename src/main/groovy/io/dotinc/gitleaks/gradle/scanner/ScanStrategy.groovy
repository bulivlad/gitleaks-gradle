package io.dotinc.gitleaks.gradle.scanner

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.RunEnvironment


/**
 * @author vladbulimac on 14.04.2022.
 */

class ScanStrategy {
    private static final def strategy = [(RunEnvironment.DOCKER): new DockerScanner(),
                                         (RunEnvironment.NATIVE): new NativeScanner()]

    private ScanStrategy() {
    }

    static Scanner getScanner(RunEnvironment environment) {
        strategy[environment]
    }

}
