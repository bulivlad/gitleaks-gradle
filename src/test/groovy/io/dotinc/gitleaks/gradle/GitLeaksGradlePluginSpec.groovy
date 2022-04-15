package io.dotinc.gitleaks.gradle

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension
import io.dotinc.gitleaks.gradle.extension.ReportExportExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author vladbulimac on 15.04.2022.
 */

class GitLeaksGradlePluginSpec extends Specification {
    static final String PLUGIN_ID = 'io.dotinc.gitleaks'
    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: PLUGIN_ID
    }

    def 'gitLeaks extension exists'() {
        expect:
        project.extensions.findByName('gitLeaks')
    }

    def "gitLeaksDetect task exists"() {
        expect:
        project.tasks.findByName(GitLeaksPlugin.DETECT_TASK)
    }

    def "gitLeaksProtect task exists"() {
        expect:
        project.tasks.findByName(GitLeaksPlugin.PROTECT_TASK)
    }

    def 'gitLeaks task has correct default values'() {
        setup:
        Task task = project.tasks.findByName(GitLeaksPlugin.DETECT_TASK)

        expect:
        task.group == 'GitLeaks scanner'
        task.description == 'Detect secrets in uncommitted code'

        project.gitLeaks.outputDirectory == "${project.rootDir.relativePath(project.buildDir)}/gitleak-report"
        project.gitLeaks.skip == false
        project.gitLeaks.failOnError == false
        project.gitLeaks.configFile == null
        project.gitLeaks.relativeSourcePath == project.rootDir.relativePath(project.projectDir)
        project.gitLeaks.format == GitLeaksExtension.Format.JSON
        project.gitLeaks.runEnvironment == GitLeaksExtension.RunEnvironment.DOCKER
        project.gitLeaks.vault.vaultPath == null
        project.gitLeaks.vault.vaultKeyFile == null
        project.gitLeaks.export.failOnError == false
        project.gitLeaks.export.vaultLookup == false
        project.gitLeaks.export.url == null
        project.gitLeaks.export.authType == ReportExportExtension.AuthType.NONE
        project.gitLeaks.export.bearerToken == null
        project.gitLeaks.export.basicUsername == null
        project.gitLeaks.export.basicPassword == null
        project.gitLeaks.export.basicToken == null
    }

    def 'tasks use correct values when extension is used'() {
        when:
        project.gitLeaks {
            outputDirectory = 'build/custom-directory'
            skip = true
            failOnError = true
            configFile = 'config/gitleaks-config.toml'
            relativeSourcePath = 'repo/specific-part/'
            format = 'CSV'
            runEnvironment = 'NATIVE'
            vault {
                vaultPath = '/home/user/vault.yml'
                vaultKeyFile = '/home/user/password.sh'
            }

            export {
                failOnError = true
                vaultLookup = true
                url = 'http://localhost/api/report'
                authType = 'basic'
                bearerToken = 'Bearer token'
                basicUsername = "basic_username"
                basicPassword = 'basic_password'
                basicToken = 'Basic token'
            }
        }

        then:
        project.gitLeaks.outputDirectory == 'build/custom-directory'
        project.gitLeaks.skip == true
        project.gitLeaks.failOnError == true
        project.gitLeaks.configFile == 'config/gitleaks-config.toml'
        project.gitLeaks.relativeSourcePath == 'repo/specific-part/'
        project.gitLeaks.format == GitLeaksExtension.Format.CSV
        project.gitLeaks.runEnvironment == GitLeaksExtension.RunEnvironment.NATIVE
        project.gitLeaks.vault.vaultPath == '/home/user/vault.yml'
        project.gitLeaks.vault.vaultKeyFile == '/home/user/password.sh'
        project.gitLeaks.export.failOnError == true
        project.gitLeaks.export.vaultLookup == true
        project.gitLeaks.export.url == 'http://localhost/api/report'
        project.gitLeaks.export.authType == ReportExportExtension.AuthType.BASIC
        project.gitLeaks.export.bearerToken == 'Bearer token'
        project.gitLeaks.export.basicUsername == 'basic_username'
        project.gitLeaks.export.basicPassword == 'basic_password'
        project.gitLeaks.export.basicToken == 'Basic token'
    }

}

