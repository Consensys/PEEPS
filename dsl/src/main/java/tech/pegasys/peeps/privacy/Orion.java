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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.privacy.rpc.OrionRpcClient;
import tech.pegasys.peeps.util.ClasspathResources;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class Orion {

  private static final Logger LOG = LogManager.getLogger();

  private static final String CONTAINER_CONFIG_FILE = "/orion.conf";

  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  private static final int ALIVE_STATUS_CODE = 200;
  private static final int CONTAINER_HTTP_RPC_PORT = 8888;
  private static final int CONTAINER_PEER_TO_PEER_PORT = 8080;

  // TODO there should be the 'latest' version
  private static final String ORION_IMAGE = "pegasyseng/orion:develop";

  private static final String CONTAINER_WORKING_DIRECTORY_PREFIX = "/opt/orion/";

  private static final AtomicLong UNIQUEIFIER = new AtomicLong();

  private final GenericContainer<?> orion;
  private final String orionNetworkAddress;
  private final OrionRpcClient jsonRpc;

  private final String nodePublicKey;

  public Orion(final OrionConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(ORION_IMAGE);
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);
    addPrivateKeys(config, container);
    addPublicKeys(config, container);

    try {
      config.write();
    } catch (final IOException e) {
      final String message =
          String.format(
              "Problem creating the Orion config file in the file system: %s, %s",
              config.getFileSystemConfigurationFile(), e.getLocalizedMessage());
      LOG.error(message);
      throw new IllegalStateException(message);
    }

    // TODO write out & bind to the container

    this.orion =
        container
            .withCommand(CONTAINER_CONFIG_FILE)
            .withFileSystemBind(config.getFileSystemConfigurationFile(), CONTAINER_CONFIG_FILE)
            .waitingFor(liveliness());

    this.orionNetworkAddress =
        String.format("http://%s:%s", config.getIpAddress(), CONTAINER_PEER_TO_PEER_PORT);

    // TODO just using the first key, selecting the identity could be an option for multi-key Orion
    this.nodePublicKey = ClasspathResources.read(config.getPublicKeys().get(0));
    this.jsonRpc = new OrionRpcClient(config.getVertx(), nodePublicKey);
  }

  public void awaitConnectivity(final Orion peer) {

    // TODO port this into the rpc client as a test payload
    final String sentMessage = "Test payload " + UNIQUEIFIER.getAndIncrement();

    final String receipt = jsonRpc.send(peer.nodePublicKey, sentMessage);
    assertThat(receipt).isNotBlank();

    final String receivedMessage = peer.jsonRpc.receive(receipt);

    assertThat(receivedMessage).isEqualTo(sentMessage);
  }

  public void start() {
    try {
      orion.start();

      jsonRpc.bind(
          orion.getContainerId(),
          orion.getContainerIpAddress(),
          orion.getMappedPort(CONTAINER_HTTP_RPC_PORT));

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(orion.getLogs());
      throw e;
    }
  }

  public void stop() {
    if (orion != null) {
      orion.stop();
    }
    if (jsonRpc != null) {
      jsonRpc.close();
    }
  }

  public String getNetworkAddress() {
    return orionNetworkAddress;
  }

  private void addPrivateKeys(
      final OrionConfiguration config, final GenericContainer<?> container) {
    for (final String key : config.getPrivateKeys()) {
      container.withClasspathResourceMapping(
          key, containerWorkingDirectory(key), BindMode.READ_ONLY);
    }
  }

  private void addPublicKeys(final OrionConfiguration config, final GenericContainer<?> container) {
    for (final String key : config.getPublicKeys()) {
      container.withClasspathResourceMapping(
          key, containerWorkingDirectory(key), BindMode.READ_ONLY);
    }
  }

  private String containerWorkingDirectory(final String relativePath) {
    return CONTAINER_WORKING_DIRECTORY_PREFIX + relativePath;
  }

  private void logContainerNetworkDetails() {
    if (orion.getNetwork() == null) {
      LOG.info("Orion Container {} has no network", orion.getContainerId());
    } else {
      LOG.info(
          "Orion Container {}, IP address: {}, Network: {}",
          orion.getContainerId(),
          orion.getContainerIpAddress(),
          orion.getNetwork().getId());
    }
  }

  private void logPortMappings() {
    LOG.info(
        "Orion Container {}, HTTP RPC port mapping: {} -> {}, p2p port mapping: {} -> {}",
        orion.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        orion.getMappedPort(CONTAINER_HTTP_RPC_PORT),
        CONTAINER_PEER_TO_PEER_PORT,
        orion.getMappedPort(CONTAINER_PEER_TO_PEER_PORT));
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private void addContainerNetwork(
      final OrionConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addContainerIpAddress(
      final OrionConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress()));
  }
}
