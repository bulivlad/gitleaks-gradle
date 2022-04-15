package io.dotinc.gitleaks.gradle.service

import io.dotinc.gitleaks.gradle.extension.ReportExportExtension.AuthType
import io.dotinc.gitleaks.gradle.extension.GitLeaksExtension.Format
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import wslite.http.HTTP
import wslite.http.auth.HTTPAuthorization
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.ContentType
import wslite.rest.RESTClient

import java.util.stream.Stream

class ReportExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultService.class);
    public static final String EXPORT__FAIL__ON__ERROR = "EXPORT_FAIL_ON_ERROR"
    public static final String EXPORT__VAULT__LOOKUP = "EXPORT_VAULT_LOOKUP"
    public static final String EXPORT__URL = "EXPORT_URL"
    public static final String EXPORT__AUTH__TYPE = "EXPORT_AUTH_TYPE"
    public static final String EXPORT__BEARER__TOKEN = "EXPORT_BEARER_TOKEN"
    public static final String EXPORT__BASIC__TOKEN = "EXPORT_BASIC_TOKEN"
    public static final String EXPORT__BASIC__USERNAME = "EXPORT_BASIC_USERNAME"
    public static final String EXPORT__BASIC__PASSWORD = "EXPORT_BASIC_PASSWORD"

    private Boolean failOnError
    private Boolean vaultLookup
    private String url
    private AuthType authType
    private String bearerToken
    private String basicToken
    private String basicUsername
    private String basicPassword

    private VaultService vaultService

    ReportExportService(def settings) {
        this.failOnError = settings.getBoolean(EXPORT__FAIL__ON__ERROR, false)
        this.vaultLookup = settings.getBoolean(EXPORT__VAULT__LOOKUP, false)
        this.url = settings.getString(EXPORT__URL)
        this.authType = AuthType.valueOf(AuthType.class, settings.getString(EXPORT__AUTH__TYPE))
        this.bearerToken = settings.getString(EXPORT__BEARER__TOKEN)
        this.basicToken = settings.getString(EXPORT__BASIC__TOKEN)
        this.basicUsername = settings.getString(EXPORT__BASIC__USERNAME)
        this.basicPassword = settings.getString(EXPORT__BASIC__PASSWORD)

        if (vaultLookup) {
            vaultService = new VaultService(settings)
            replaceVaultVariables()
        }
    }

    public void export(File content,  Format format) {
        LOGGER.info("Start exporting file ${content.getName()} with format ${format.toString()}.")
        try {
            LOGGER.debug("Start handling ${format} file.")
            exportInternal(content, format)

            LOGGER.info("Report ${content.getName()} exported to ${url}")
        } catch (Exception ex) {
            LOGGER.error("Failed to export the report ${content.getName()} to ${url}", ex)
            if (failOnError) {
                throw ex
            }
        }
    }

    private void exportInternal(File content, Format format) {
        LOGGER.debug("Building rest client for ${content.getName()} to ${this.url}")

        RESTClient client = new RESTClient(this.url)
        client.defaultAcceptHeader = ContentType.JSON

        def authorization = buildAuthHeader(content.getName())
        if (authorization != null) {
            client.authorization = authorization
        }

        def contentType = getContentTypeFromFileFormat(format)

        LOGGER.debug("Identified Content-Type ${contentType} based on ${format.toString()} file format")

        client.post() {
            type contentType
            text content.getText("UTF-8")
        }
    }

    private def buildAuthHeader(String fileName) {
        if (authType == AuthType.BEARER) {
            if (StringUtils.isBlank(bearerToken)) {
                LOGGER.error("Bearer token not found when exporting report ${fileName}")
            }
            def token = bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer ${bearerToken}"

            LOGGER.trace("Building authorization header with ${token}")

            return buildHttpAuthorization(token)
        }
        if (authType == AuthType.BASIC) {
            if (StringUtils.isNotBlank(basicToken)) {
                def token = basicToken.startsWith("Basic") ? basicToken : "Basic ${basicToken}"

                LOGGER.trace("Building authorization header with ${token}")

                return buildHttpAuthorization(token)
            }
            if (StringUtils.isNotBlank(basicUsername) && StringUtils.isNotBlank(basicPassword)) {
                LOGGER.trace("Building authorization header with ${basicUsername}:${basicPassword}")

                return new HTTPBasicAuthorization(basicUsername, basicPassword)
            }
            LOGGER.error("Basic authentication details not found when exporting report ${fileName}")
            return null
        }
        return null
    }

    private def buildHttpAuthorization(String token) {
        return new HTTPAuthorization() {
            @Override
            void authorize(Object conn) {
                conn.addRequestProperty(HTTP.AUTHORIZATION_HEADER, token)
            }
        }
    }

    private def getContentTypeFromFileFormat(Format format) {
        LOGGER.debug("Mapping ${format.toString()} file content to Content-Type header")
        switch (format) {
            case Format.JSON: return ContentType.JSON
            case Format.CSV: return "text/csv"
            case Format.SARIF: return ContentType.JSON
            default: return ContentType.ANY
        }
    }

    private void replaceVaultVariables() {
        LOGGER.debug("Replacing placeholders with vault values")

        this.bearerToken = getVaultValue(this.bearerToken)
        this.basicToken = getVaultValue(this.basicToken)
        this.basicUsername = getVaultValue(this.basicUsername)
        this.basicPassword = getVaultValue(this.basicPassword)

        LOGGER.debug("Finish replacing placeholders with vault values")
    }

    private def getVaultValue(String key) {
        LOGGER.debug("Checking key ${key} against regex")
        def keyRegex = (key =~ "\\{\\{(.+)}}")

        if (keyRegex.matches()) {
            def value = vaultService.getValue(keyRegex.group(1).trim())
            LOGGER.debug("Replacing value for '${key}' with '${value}'")
            return value
        }

        LOGGER.debug("Key ${key} not matching")
        return key
    }
}
