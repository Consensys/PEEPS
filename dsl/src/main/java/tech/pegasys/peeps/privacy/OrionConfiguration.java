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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;

public class OrionConfiguration {

  private static final Logger LOG = LogManager.getLogger();
  private static final int HTTP_RPC_PORT = 8888;
  private static final int PEER_TO_PEER_PORT = 8080;

  private final List<String> privKeys;
  private final List<String> pubKeys;
  private final List<String> bootnodeUrls;
  private final String ipAddress;
  private final Path fileSystemConfigurationFile;

  // TODO move these out, they are not related to the node, but test container setups
  private final Network containerNetwork;
  private final Vertx vertx;

  public OrionConfiguration(
      final List<String> privKeys,
      final List<String> pubKeys,
      final List<String> bootnodeUrls,
      final String ipAddress,
      final Network containerNetwork,
      final Vertx vertx,
      final Path fileSystemConfigurationFile) {
    this.privKeys = privKeys;
    this.pubKeys = pubKeys;
    this.bootnodeUrls = bootnodeUrls;
    this.ipAddress = ipAddress;
    this.containerNetwork = containerNetwork;
    this.vertx = vertx;
    this.fileSystemConfigurationFile = fileSystemConfigurationFile;
  }

  public String getFileSystemConfigurationFile() {
    try {
      return fileSystemConfigurationFile.toUri().toURL().getPath();
    } catch (final MalformedURLException e) {
      throw new IllegalStateException(
          "Problem forming a URL from a URI from " + fileSystemConfigurationFile.toUri());
    }
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Network getContainerNetwork() {
    return containerNetwork;
  }

  public List<String> getPrivateKeys() {
    return privKeys;
  }

  public List<String> getPublicKeys() {
    return pubKeys;
  }

  public Vertx getVertx() {
    return vertx;
  }

  public void write() throws IOException {

    final StringBuilder content = new StringBuilder();
    content.append(String.format("nodeUrl = \"http://%s:%d\"\n", ipAddress, PEER_TO_PEER_PORT));
    content.append(String.format("clientUrl = \"http://%s:%d\"\n", ipAddress, HTTP_RPC_PORT));
    content.append(String.format("nodeport = %d\n", PEER_TO_PEER_PORT));
    content.append(String.format("clientport = %d\n", HTTP_RPC_PORT));
    content.append(String.format("publickeys = [%s]\n", flatten(pubKeys)));
    content.append(String.format("privatekeys = [%s]\n", flatten(privKeys)));

    content.append("nodenetworkinterface = \"0.0.0.0\"\n");
    content.append("clientnetworkinterface = \"0.0.0.0\"\n");

    if (bootnodeUrls != null) {
      content.append(String.format("othernodes  = [%s]\n", flatten(bootnodeUrls)));
    }

    // TODO move to utils?
    LOG.info(
        "Creating Orion config at: {}, with contents:\n{}",
        content.toString(),
        fileSystemConfigurationFile);
    Files.write(
        fileSystemConfigurationFile,
        content.toString().getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE);
  }

  private String flatten(final List<String> values) {
    return values.stream().map(k -> "\"" + k + "\"").collect(Collectors.joining(","));
  }
}
