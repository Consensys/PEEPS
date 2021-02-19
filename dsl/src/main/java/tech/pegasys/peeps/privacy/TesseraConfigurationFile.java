/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.peeps.privacy;

import static java.util.Map.entry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TesseraConfigurationFile {

  private static final Logger LOG = LogManager.getLogger();
  // TODO JF configure working dir in Tessera?
  private static final String CONTAINER_WORKING_DIRECTORY_PREFIX = "/opt/tessera/";
  private static final int HTTP_RPC_PORT = 8888;
  private static final int THIRD_PARTY_RPC_PORT = 8890;
  private static final int PEER_TO_PEER_PORT = 8080;

  public static void write(TransactionManagerConfiguration config) {
    final Map<String, Object> content = new HashMap<>();
    content.put("useWhiteList", false);
    content.put(
        "jdbc",
        Map.ofEntries(
            entry("username", "sa"),
            entry("password", ""),
            entry("url", "jdbc:h2:/tmp/tessera;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0"),
            entry("autoCreateTables", true)));
    content.put(
        "serverConfigs",
        List.of(
            Map.ofEntries(
                entry("app", "ThirdParty"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + THIRD_PARTY_RPC_PORT),
                entry("communicationType", "REST")),
            Map.ofEntries(
                entry("app", "Q2T"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + HTTP_RPC_PORT),
                entry("communicationType", "REST")),
            Map.ofEntries(
                entry("app", "P2P"),
                entry("enabled", true),
                entry("serverAddress", "http://localhost:" + PEER_TO_PEER_PORT),
                entry("communicationType", "REST"))));

    // TODO JF this should be configurable
    content.put("mode", "orion");
    content.put("alwaysSendTo", List.of());

    // TODO JF configure bootnodes
    // TODO JF bootnodes as empty list of optional? why both?
    final List<Map<String, String>> peers = new ArrayList<>();
    final List<String> bootnodeUrls =
        config.getBootnodeUrls().isEmpty() ? List.of() : config.getBootnodeUrls().get();
    if (bootnodeUrls.isEmpty()) {
      peers.add(Map.of("url", "http://127.0.0.1:" + PEER_TO_PEER_PORT));
    } else {
      config.getBootnodeUrls().get().forEach(bootnodeUrl -> peers.add(Map.of("url", bootnodeUrl)));
    }

    content.put("peer", peers);

    // TODO JF where do we get this from?

    // TODO JF return a single list of private/public key data to avoid iterating lists separately
    final List<Map<String, String>> keyData = new ArrayList<>();
    for (int i = 0; i < config.getPrivateKeys().size(); i++) {
      keyData.add(
          // TODO JF create location using Path
          Map.ofEntries(
              entry(
                  "privateKeyPath",
                  CONTAINER_WORKING_DIRECTORY_PREFIX + "/" + config.getPrivateKeys().get(i).get()),
              entry(
                  "publicKeyPath",
                  CONTAINER_WORKING_DIRECTORY_PREFIX + "/" + config.getPublicKeys().get(i).get())));
    }

    content.put("keys", Map.of("passwords", List.of(), "keyData", keyData));
    LOG.info(
        "Creating Tessera config file\n\tLocation: {} \n\tContents: {}",
        config.getFileSystemConfigurationFile(),
        content.toString());

    try {
      final String configContent = new ObjectMapper().writeValueAsString(content);
      LOG.info("Tessera configuration file contents = {}", configContent);
      Files.write(
          config.getFileSystemConfigurationFile(),
          configContent.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE);
    } catch (final IOException e) {
      final String message =
          String.format(
              "Problem creating the Tessera config file in the file system: %s, %s",
              config.getFileSystemConfigurationFile(), e.getLocalizedMessage());
      LOG.error(message);
      throw new IllegalStateException(message);
    }
  }
}
