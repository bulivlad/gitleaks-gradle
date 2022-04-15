package io.dotinc.gitleaks.gradle


import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
/**
 * @author vladbulimac on 15.04.2022.
 */

class GitLeaksPluginIntegSpec extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "Plugin can be added"() {
        given:
        buildFile << """
            plugins {
                id 'io.dotinc.gitleaks'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks')
                .withPluginClasspath()
                .forwardOutput()
                .build()

        then:
        result.output.contains("$GitLeaksPlugin.DETECT_TASK")
        result.output.contains("$GitLeaksPlugin.PROTECT_TASK")
    }

    def "scan is skipped when skip = true is configured"() {
        given:
        buildFile << """
            plugins {
                id 'io.dotinc.gitleaks'
            }
            apply plugin: 'java'
            
            sourceCompatibility = 1.5
            version = '1.0'
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            dependencies {
                compile group: 'commons-collections', name: 'commons-collections', version: '3.2'
            }
            
            gitLeaks {
                skip = true
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(GitLeaksPlugin.DETECT_TASK)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .build()

        then:
        result.task(":$GitLeaksPlugin.DETECT_TASK").outcome == SUCCESS
        result.output.contains("Skipping git-leaks scan.")
    }

    def "task completes successfully when error and failOnError = false"() {
        given:
        buildFile << """
            plugins {
                id 'io.dotinc.gitleaks'
            }
            apply plugin: 'java'
            
            sourceCompatibility = 1.5
            version = '1.0'
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            dependencies {
                implementation group: 'commons-collections', name: 'commons-collections', version: '3.2'
            }
            
            gitLeaks {
                failOnError = false
                configFile = 'doesNotExist'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(GitLeaksPlugin.DETECT_TASK)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .build()

        then:
        result.task(":$GitLeaksPlugin.DETECT_TASK").outcome == SUCCESS
    }

    def "task fails when error and failOnError = true"() {
        given:
        buildFile << """
            plugins {
                id 'io.dotinc.gitleaks'
            }
            apply plugin: 'java'
            
            sourceCompatibility = 1.5
            version = '1.0'
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            dependencies {
                implementation group: 'commons-collections', name: 'commons-collections', version: '3.2'
            }
            
            gitLeaks {
                failOnError = true
                configFile = 'doesNotExist'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(GitLeaksPlugin.DETECT_TASK)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .buildAndFail()

        then:
        result.task(":$GitLeaksPlugin.DETECT_TASK").outcome == FAILED
    }
}
