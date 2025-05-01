package org.consensusj.jsonrpc.cli.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class JsonRpcConfigFile {
    private static final String APP_NAME = "jsonrpc";
    private static final String CONFIG_FILE_NAME = "config.toml";
    private static final Logger log = LoggerFactory.getLogger(JsonRpcConfigFile.class);
    protected final File configFile;
    protected final TomlMapper mapper;

    public JsonRpcConfigFile(Path filePath) {
        configFile = filePath.toFile();
        mapper = new TomlMapper();
    }

    public static JsonRpcConfigFile fromDefaultConfigFile() {
        return new JsonRpcConfigFile(XdgDataDir.getPath(APP_NAME).resolve(CONFIG_FILE_NAME));
    }

    public Path path() {
        return configFile.toPath();
    }

    public boolean exists() {
        log.info("Checking if config file exists at {} ", configFile.getAbsolutePath());
        return configFile.exists();
    }

    public JsonRpcServerConfigEntry readDefault() {
        return readOne("default");
    }
    
    /**
     * Read one named JsonRpcServerConfigEntry after reading the file into a JsonNode
     * @param index name of the consensus source to read
     * @return An object containing info on the consensus source
     */
    public JsonRpcServerConfigEntry readOne(String index) {
        JsonRpcServerConfigEntry config = null;
        try {
            var rootNode = mapper.readValue(configFile, JsonNode.class);
            var configEntryNode = rootNode.get(index);
            var networkString = configEntryNode.get("bitcoin-network").asText();
            config = new JsonRpcServerConfigEntry(networkString,
                    URI.create(configEntryNode.get("uri").asText()),
                    configEntryNode.get("username").asText(),
                    configEntryNode.get("password").asText());
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
        log.info("config: {},{},{}", config.getBitcoinNetwork(), config.getUri(), config.getUsername());
        return config;
    }
}
