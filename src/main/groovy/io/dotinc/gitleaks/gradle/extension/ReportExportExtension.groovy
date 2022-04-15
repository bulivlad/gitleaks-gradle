package io.dotinc.gitleaks.gradle.extension

/**
 * The configuration for the report export extensions.
 */
class ReportExportExtension {
    Boolean failOnError = false
    Boolean vaultLookup = false
    String url
    AuthType authType = AuthType.NONE
    String bearerToken
    String basicUsername
    String basicPassword
    String basicToken

    public enum AuthType {
        /**
         * No authentication required
         */
        NONE,
        /**
         * Basic authentication required
         */
        BASIC,
        /**
         * Bearer authentication required
         */
        BEARER
    }
}
