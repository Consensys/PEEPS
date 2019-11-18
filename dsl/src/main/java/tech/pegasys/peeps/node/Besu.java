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
package tech.pegasys.peeps.node;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class Besu {

  // TODO refactor?
  private static final int DEFAULT_HTTP_RPC_PORT = 8545;
  private static final int DEFAULT_WS_RPC_PORT = 8546;

  private static final Logger LOG = LogManager.getLogger();
  private static final String BESU_IMAGE = "hyperledger/besu:latest";

  private static final String CONTAINER_GENESIS_FILE = "/etc/besu/genesis.json";

  private final GenericContainer<?> besu;

  private static final String CONTAINER_PRIVACY_PUBLIC_KEY_FILE = "/etc/besu/privacy_public_key";

  public Besu(final NodeConfiguration config) {
    final String genesisFilePath = genesisFilePath(config.getGenesisFilePath());

    // TODO enclave key to config
    final String privacyPublicKeyFile = "enclave_key.pub";

    final List<String> commandLineOptions =
        Lists.newArrayList(
            "--genesis-file",
            CONTAINER_GENESIS_FILE,
            "--logging",
            "DEBUG",
            "--miner-enabled",
            "--miner-coinbase",
            "1b23ba34ca45bb56aa67bc78be89ac00ca00da00",
            "--host-whitelist",
            "*",
            "--rpc-http-enabled",
            "--rpc-ws-enabled",
            "--rpc-http-apis",
            "ETH,NET,WEB3,EEA",
            "--privacy-enabled",
            "--privacy-public-key-file",
            CONTAINER_PRIVACY_PUBLIC_KEY_FILE);

    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--rpc-http-cors-origins", cors)));

    LOG.debug("besu command line {}", config);

    // TODO no magic strings or status codes
    this.besu =
        new GenericContainer<>(BESU_IMAGE)
            .withCommand(commandLineOptions.toArray(new String[0]))
            .withExposedPorts(DEFAULT_HTTP_RPC_PORT, DEFAULT_WS_RPC_PORT)
            .withFileSystemBind(genesisFilePath, CONTAINER_GENESIS_FILE, BindMode.READ_ONLY)
            .withFileSystemBind(
                privacyPublicKeyFile, CONTAINER_PRIVACY_PUBLIC_KEY_FILE, BindMode.READ_ONLY)
            .waitingFor(
                Wait.forHttp("/liveness").forStatusCode(200).forPort(DEFAULT_HTTP_RPC_PORT));
  }

  public void start() {
    besu.start();
  }

  public void stop() {
    besu.stop();
  }

  public void awaitConnectivity(final Besu peer) {
    // TODO assert that connection to peer within say 10s occurs
  }

  // TODO file handling stuff should be in a util class
  private String genesisFilePath(final String filename) {
    final URL resource = Besu.class.getResource(filename);

    if (resource == null) {
      final String message = String.format("File '%s' not found on classpath", filename);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    return resourceFileName(resource);
  }

  private String resourceFileName(final URL resource) {
    try {
      return URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8.name());
    } catch (final UnsupportedEncodingException ex) {
      LOG.error("Unsupported encoding used to decode {}, filepath.", resource);
      throw new RuntimeException("Illegal string decoding");
    }
  }
}
