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
package tech.pegasys.peeps;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class Orion {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  private static final int ALIVE_STATUS_CODE = 200;
  private static final int CONTAINER_HTTP_CLIENT_PORT = 8888;
  private static final int CONTAINER_HTTP_NODE_PORT = 8080;

  // TODO there should be a latest tagged version
  private static final String ORION_IMAGE = "pegasyseng/orion:develop";

  private final GenericContainer<?> orion;

  public Orion() {

    final GenericContainer<?> container = new GenericContainer<>(ORION_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    LOG.debug("Orion command line {}", commandLineOptions);

    this.orion =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());

    // TODO pass in a temporary test directory (create in Network)
    // TODO use this / private method
    final List<String> privKeys = null;
    final List<String> pubKeys = null;
    final List<String> bootnodeUrls = null;
    final String networkIpAddress = null;
    new OrionConfig(privKeys, pubKeys, bootnodeUrls, networkIpAddress);
    // TODO write out & bind to the container
  }

  public void awaitConnectivity(final Orion peer) {
    // TODO assert that connection to peer within say 10s occurs
  }

  public void start() {
    try {
      orion.start();

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      //      logPortMappings();
      //      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(orion.getLogs());
      throw e;
    }
  }

  public void stop() {
    orion.stop();
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "INFO",
        "--miner-enabled",
        "--miner-coinbase",
        "1b23ba34ca45bb56aa67bc78be89ac00ca00da00",
        "--host-whitelist",
        "*",
        "--rpc-http-enabled",
        "--rpc-ws-enabled",
        "--rpc-http-apis",
        "ADMIN,ETH,NET,WEB3,EEA");
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_CLIENT_PORT);
  }
}
