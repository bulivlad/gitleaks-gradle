GitLeaks-Gradle
=========

![Build](https://github.com/bulivlad/gitleaks-gradle/workflows/Build/badge.svg)

The gitleaks gradle plugin allows projects to scan commited and uncommited code for leaked secrets and passwords.

## Current Release
The latest version is
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/dotinc/gitleaks/io.dotinc.gitleaks.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradle-plugin)](https://plugins.gradle.org/plugin/io.dotinc.gitleaks)

## Usage
Below are the quick start instructions. Please see the table [here](https://github.com/bulivlad/gitleaks-gradle#configuration) for full configuration

### Step 1, Apply gitleaks gradle plugin

Install from Maven central repo

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.dotinc:gitleaks-gradle:0.1.0'
    }
}

apply plugin: 'io.dotinc.gitleaks'
```

### Step 2, Run gradle task

Once gradle plugin applied, run following gradle tasks:

```
gradle gitLeaksDetect
```
or
```
gradle gitLeaksProtect
```

The reports will be generated automatically under `build/gitleak-report` directory.

## Configuration
See the table for full available configuration
| Options | Description | Default value | Available options ||
| ------------- | :------------- | :-----| :-----|:-----|
| skip | Flag to enable/disable scanning | false | true/false |
| failOnError | Flag indicating whether the gradle build should fail or not if the scan fails | false | true/false |
| configFile | The path to the configuration file relative to the root folder | https://github.com/zricethezav/gitleaks/blob/master/config/gitleaks.toml | |
| format | The format of the report | JSON | JSON/CSV/SARIF/HTML |
| runEnvironment | Configures in which environment the scan should run | DOCKER | DOCKER/NATIVE |
| maskSensitiveData | Flag indicating whether the sensitive data should be masked in the report | true | true/false |
| vault | Vault configuration block | | |
|| **Option** | **Description** | **Default value** | **Available options** |
|| vaultPath | The path to the vault file |||
|| vaultKeyFile | The path to the bash script outputting the vault key |||
| export | Report export configuration block | | |
|| **Option** | **Description** | **Default value** | **Available options** |
|| failOnError | Flag indicating whether the gradle build should fail or not if the export fails | false | true/false |
|| vaultLookup | Flag indicating whether the variables should be looked up in the vault file |||
|| url | The server url |||
|| authType | The authentication type used for the server | NONE | NONE/BASIC/BEARER |
|| bearerToken | The token used for BEARER authentication type |||
|| basicUsername | The username used for BASIC authentication type |||
|| basicPassword | The password used for BASIC authentication type |||
|| basicToken | The token used for BASIC authentication type - alternative to declaring username and password |||

| |  |
| --- | --- |
| NOTE:  | The vault block can only be used to lookup variables defined in export block |
| | To mark the variables for vault lookup you need to use following notation `{{ path.in.vault.file }}` e.g. ```basicUsername = {{ auth.basic_username }}```|


## FAQ

### How to customize the report directory?

By default, all reports will be placed under `build/reports` folder, to change the default reporting folder name modify the configuration section like this:

```groovy
subprojects {
    apply plugin: 'io.dotinc.gitleaks'

    gitLeaks {
        outputDirectory = "build/report"
    }
}
```

### How to enable report export to remote server?

By default, the report will be generated and only stored on the local machine. To export the report you need to configure the `export` block

```groovy
subprojects {
    apply plugin: 'io.dotinc.gitleaks'

    gitLeaks {
        export {
            url = 'http://server-url/api/report'
            authType = 'basic'
            basicUsername = "USERNAME"
            basicPassword = "PASSWORD"
        }
    }
}
```