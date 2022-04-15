package io.dotinc.gitleaks.gradle.service


import groovy.yaml.YamlSlurper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VaultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultService.class);
    public static final String VAULT__FILE__PATH = "VAULT_FILE_PATH"
    public static final String VAULT__KEYFILE = "VAULT_KEYFILE"

    private String vaultPath
    private String vaultKeyFile
    private final Object decrypted

    VaultService(def settings) {
        def filePath = settings.getString(VAULT__FILE__PATH)
        def passPhrase = settings.getString(VAULT__KEYFILE)

        if (StringUtils.isNotBlank(filePath) && StringUtils.isNotBlank(passPhrase)) {
            this.vaultPath = filePath
            this.vaultKeyFile = passPhrase
            this.decrypted = decrypt()
        } else {
            decrypted = null
        }
    }

    private def decrypt() {
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        def proc = ["/bin/bash", "-c", "ansible-vault view ${vaultPath} --vault-password-file ${vaultKeyFile}"].execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000)
        if (serr.length() > 0) {
            LOGGER.error("Decryption of ansible-vault file in path ${vaultPath} ended with error ${serr.toString()}")
            throw new GroovyRuntimeException(serr.toString())
        }

        def ys = new YamlSlurper()
        return ys.parseText(sout.toString())
    }

    public String getValue(String key) {
        if (decrypted == null) {
            LOGGER.error("Trying to retrieve key ${key} from vault before initializing it. Please consider declaring vault block in your gradle file!")
            return null
        }
        return getValueInternal(key)
    }

    private getValueInternal(String params){
        return params.split(/\./).inject(this.decrypted) { map, key -> map.get(key) }
    }
}
