package io.dotinc.gitleaks.gradle

import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension
import io.dotinc.gitleaks.gradle.task.Detect
import io.dotinc.gitleaks.gradle.task.Protect
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

/**
 * @author vladclaudiubulimac on 08/09/2020.
 */

class GitLeaksPlugin  implements Plugin<Project> {

    static final GradleVersion MINIMUM_GRADLE_VERSION = GradleVersion.version("4.0")
    static final GradleVersion REGISTER_TASK_GRADLE_VERSION = GradleVersion.version("4.9")

    public static final String PROTECT_TASK = 'gitLeaksProtect'
    public static final String DETECT_TASK = 'gitLeaksDetect'

    private static final String LEAKS_EXTENSION_NAME = "gitLeaks"

    @Override
    void apply(Project project) {
        initializeConfigurations(project)
        registerTasks(project)
        checkGradleVersion(project)
    }

    void initializeConfigurations(Project project) {
        project.extensions.create(LEAKS_EXTENSION_NAME, GitLeaksExtension, project)
    }

    void registerTasks(Project project) {
        if (REGISTER_TASK_GRADLE_VERSION.compareTo(GradleVersion.current())<=0) {
            project.tasks.register(PROTECT_TASK, Protect)
            project.tasks.register(DETECT_TASK, Detect)
        } else {
            project.task(PROTECT_TASK, type: Protect)
            project.task(DETECT_TASK, type: Detect)
        }
    }

    void checkGradleVersion(Project project) {
        if (project != null && MINIMUM_GRADLE_VERSION.compareTo(GradleVersion.current()) > 0) {
            if (project.plugins.contains("com.android.build.gradle.AppPlugin")) {
                throw new GradleException("Detected ${GradleVersion.current()}; the gitleaks-gradle " +
                        "plugin requires ${MINIMUM_GRADLE_VERSION} or higher when analyzing Android projects.")
            } else {
                project.logger.warn("Detected ${GradleVersion.current()}; while the gitleaks-gradle " +
                        "plugin will work it is recommended that you upgrade to ${MINIMUM_GRADLE_VERSION} or higher.")
            }
        }
    }
}
