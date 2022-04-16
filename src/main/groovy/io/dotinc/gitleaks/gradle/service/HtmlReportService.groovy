package io.dotinc.gitleaks.gradle.service

import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Project


/**
 * @author vladbulimac on 15.04.2022.
 */

class HtmlReportService {
    private static final String PROJECT_NAME_PLACEHOLDER = '{projectName}'
    private static final String PLUGIN_VERSION_PLACEHOLDER = '{pluginVersion}'
    private static final String CURRENT_TIME_PLACEHOLDER = '{currentTime}'
    private static final String LEAKS_FOUND_PLACEHOLDER = '{leaksFound}'
    private static final String TABLE_HEADER_PLACEHOLDER = '{tableHeader}'
    private static final String TABLE_BODY_PLACEHOLDER = '{tableBody}'
    private Project project
    private static final def CUSTOM_SORTER = { a, b, order=[ 'RuleID', 'Description', 'File', 'Secret', 'Match', 'StartLine', 'EndLine', 'StartColumn', 'EndColumn', 'Entropy', 'Commit', 'Author', 'Message', 'Date' , 'Email', 'Tags'] ->
        order.indexOf( a ) <=> order.indexOf( b )
    }

    HtmlReportService(Project project) {
        this.project = project
    }

    String generateReportFromJSON(File jsonFile) {
        def htmlText = getClass().getResourceAsStream('/html-report-template.html').getText()

        def json = new JsonSlurper().parseText(jsonFile.text)

        htmlText = htmlText.replace(PROJECT_NAME_PLACEHOLDER, project.name)
        def pluginConfig = project.buildscript.configurations.classpath.resolvedConfiguration.resolvedArtifacts.collect {
            it.moduleVersion.id }.findAll { it.name == 'gitleaks-gradle' }
        def pluginVersion = pluginConfig != null ? pluginConfig.first().version : null
        htmlText = htmlText.replace(PLUGIN_VERSION_PLACEHOLDER, pluginVersion.toString())
        htmlText = htmlText.replace(CURRENT_TIME_PLACEHOLDER, new Date().toString())
        htmlText = htmlText.replace(LEAKS_FOUND_PLACEHOLDER, json.size().toString())

        if (json.size() > 0) {
            def jsonObjectKeys = ((Map) json[0]).keySet().sort( CUSTOM_SORTER )
            def sb = new StringBuilder()
            jsonObjectKeys.each {
                sb.append("<th>")
                sb.append(it)
                sb.append("</th>")
            }
            htmlText = htmlText.replace(TABLE_HEADER_PLACEHOLDER, sb.toString())
            sb = new StringBuilder()
            json.each { object ->
                sb.append("<tr>")

                jsonObjectKeys.each { key ->
                    def value = object.get(key)
                    value = StringUtils.isNotBlank(value.toString()) ? value.toString() : 'N/A'
                    sb.append("<td>")
                    sb.append(value)
                    sb.append("</td>")
                }
                sb.append("</tr>")
            }
            htmlText = htmlText.replace(TABLE_BODY_PLACEHOLDER, sb.toString())
        } else {
            return null
        }

        return htmlText
    }
}
