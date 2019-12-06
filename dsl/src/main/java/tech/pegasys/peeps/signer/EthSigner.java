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
package tech.pegasys.peeps.signer;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class EthSigner {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/upcheck";
  private static final int ALIVE_STATUS_CODE = 200;

  private static final String ETH_SIGNER_IMAGE = "pegasyseng/ethsigner:latest";
  private static final String CONTAINER_DATA_PATH = "/ethsigner/tmp/";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;

  // TODO need a rpcClient to send stuff to the signer
  private final GenericContainer<?> ethSigner;

  public EthSigner(final EthSignerConfiguration config) {

    final GenericContainer<?> container = new GenericContainer<>(ETH_SIGNER_IMAGE);
    final List<String> commandLineOptions = standardCommandLineOptions();

    addChainId(config, commandLineOptions);
    addDownstreamPort(config, commandLineOptions);
    addDownstreamHost(config, commandLineOptions);
    addContainerNetwork(config, container);
    addContainerIpAddress(config, container);

    this.ethSigner =
        container.withCommand(commandLineOptions.toArray(new String[0])).waitingFor(liveliness());
  }

  public void start() {
    try {
      ethSigner.start();

      // TODO create signer to send things to EthSigner

      // TODO validate the node has the expected state, e.g. consensus, genesis, networkId,
      // protocol(s), ports, listen address

      logPortMappings();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(ethSigner.getLogs());
      throw e;
    }
  }

  public void stop() {
    if (ethSigner != null) {
      ethSigner.stop();
    }
  }

  public String deployContract(final String binary) {

    final String receiptHash = null;
    // TODO code

    return receiptHash;
  }

  public String getTransactionReceipt(final String receiptHash) {
    final String receipt = null;
    // TODO code

    return receipt;
  }

  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  private List<String> standardCommandLineOptions() {
    return Lists.newArrayList(
        "--logging",
        "INFO",
        "--data-path",
        CONTAINER_DATA_PATH,
        "--http-listen-host",
        "0.0.0.0",
        "--http-listen-port",
        String.valueOf(CONTAINER_HTTP_RPC_PORT));
  }

  private void logContainerNetworkDetails() {
    if (ethSigner.getNetwork() == null) {
      LOG.info("EthSigner Container {} has no network", ethSigner.getContainerId());
    } else {
      LOG.info(
          "EthSigner Container {}, IP address: {}, Network: {}",
          ethSigner.getContainerId(),
          ethSigner.getContainerIpAddress(),
          ethSigner.getNetwork().getId());
    }
  }

  private void logPortMappings() {
    LOG.info(
        "EthSigner Container {}, HTTP RPC port mapping: {} -> {}",
        ethSigner.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        ethSigner.getMappedPort(CONTAINER_HTTP_RPC_PORT));
  }

  private void addChainId(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--chain-id");
    commandLineOptions.add(String.valueOf(config.getChainId()));
  }

  private void addDownstreamPort(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--downstream-http-port");
    commandLineOptions.add(String.valueOf(config.getDownstreamPort()));
  }

  private void addDownstreamHost(
      final EthSignerConfiguration config, final List<String> commandLineOptions) {
    commandLineOptions.add("--downstream-http-host");
    commandLineOptions.add(config.getDownstreamHost());
  }

  private void addContainerNetwork(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withNetwork(config.getContainerNetwork());
  }

  private void addContainerIpAddress(
      final EthSignerConfiguration config, final GenericContainer<?> container) {
    container.withCreateContainerCmdModifier(
        modifier -> modifier.withIpv4Address(config.getIpAddress()));
  }
}
