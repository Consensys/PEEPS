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

import tech.pegasys.peeps.node.rpc.JsonRpcRequest;
import tech.pegasys.peeps.node.rpc.JsonRpcRequestId;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public class Besu {

  private static final Logger LOG = LogManager.getLogger();

  private static final String AM_I_ALIVE_ENDPOINT = "/liveness";
  private static final int ALIVE_STATUS_CODE = 200;

  private static final String BESU_IMAGE = "hyperledger/besu:latest";
  private static final String LOCALHOST = "localhost";
  private static final int CONTAINER_HTTP_RPC_PORT = 8545;
  private static final int CONTAINER_WS_RPC_PORT = 8546;
  private static final int CONTAINER_P2P_PORT = 30303;
  private static final String CONTAINER_GENESIS_FILE = "/etc/besu/genesis.json";
  private static final String CONTAINER_PRIVACY_PUBLIC_KEY_FILE =
      "/etc/besu/privacy_public_key.pub";
  private static final String CONTAINER_NODE_PRIVATE_KEY_FILE = "/etc/besu/keys/key.priv";

  private final GenericContainer<?> besu;
  private WebClient jsonRpc;

  public Besu(final NodeConfiguration config) {

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
            "--p2p-host",
            config.getIpAddress(),
            "--rpc-http-enabled",
            "--rpc-ws-enabled",
            "--rpc-http-apis",
            "ADMIN,ETH,NET,WEB3,EEA",
            "--privacy-enabled",
            "--privacy-public-key-file",
            CONTAINER_PRIVACY_PUBLIC_KEY_FILE);

    GenericContainer<?> container = besuContainer(config);

    // TODO move the other bonds & args out e.g. genesis & encalve

    // TODO refactor these into private helpers
    config
        .getCors()
        .ifPresent(
            cors -> commandLineOptions.addAll(Lists.newArrayList("--rpc-http-cors-origins", cors)));

    config
        .getNodePrivateKeyFile()
        .ifPresent(
            file -> {
              container.withFileSystemBind(
                  file, CONTAINER_NODE_PRIVATE_KEY_FILE, BindMode.READ_ONLY);
              commandLineOptions.addAll(
                  Lists.newArrayList("--node-private-key-file", CONTAINER_NODE_PRIVATE_KEY_FILE));
            });

    config
        .getBootnodeEnodeAddress()
        .ifPresent(enode -> commandLineOptions.addAll(Lists.newArrayList("--bootnodes", enode)));

    LOG.debug("besu command line {}", config);

    this.besu =
        container
            .withCreateContainerCmdModifier(
                modifier -> modifier.withIpv4Address(config.getIpAddress()))
            .withCommand(commandLineOptions.toArray(new String[0]))
            .waitingFor(liveliness());
  }

  public void start() {
    try {
      besu.start();
      logHttpRpcPortMapping();
      logWsRpcPortMapping();
      logPeerToPeerPortMapping();
      logContainerNetworkDetails();
    } catch (final ContainerLaunchException e) {
      LOG.error(besu.getLogs());
      throw e;
    }
  }

  public void stop() {
    besu.stop();
  }

  public void awaitConnectivity(final Besu peer) {
    // TODO assert that connection to peer within say 10s occurs

    final String info = nodeInfo();

    //   final String info = nodeInfoOkHttp();
  }

  // TODO no more magic strings!
  private String nodeInfo() {
    final JsonRpcRequest request =
        new JsonRpcRequest("2.0", "admin_nodeInfo", new Object[0], new JsonRpcRequestId(1));

    CompletableFuture<String> info = new CompletableFuture<String>();

    final String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"admin_nodeInfo\",\"params\":[],\"id\":1}";

    //    final String json = Json.encode(request);
    LOG.info("Request: {}", json);

    // TODO the setChunking(false) might be doing in vs the HttpClient exmaples

    // TODO Json mapper instance
    jsonRpcWebClient()
        .post("/")
        .sendJson(
            json,
            result -> {
              if (result.succeeded()) {
                final String body = result.result().bodyAsString();
                LOG.info("Got back: {}, {}", result.result().statusCode(), body);
                info.complete(body);
              } else {
                LOG.error("Querying 'admin_nodInfo failed", result.cause());
                info.completeExceptionally(result.cause());
              }
            });

    try {
      return info.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();

      throw new RuntimeException("Failed to receive a response from `admin_nodeInfo`", e);
    }
  }

  public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private String nodeInfoOkHttp() {

    OkHttpClient client = new OkHttpClient();

    final String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"admin_nodeInfo\",\"params\":[],\"id\":1}";

    try {
      Response response =
          client
              .newCall(
                  new Request.Builder()
                      .url(
                          String.format(
                              "http://%s:%s/",
                              besu.getContainerIpAddress(),
                              besu.getMappedPort(CONTAINER_HTTP_RPC_PORT)))
                      .post(RequestBody.create(JSON, json))
                      .build())
              .execute();

      LOG.info(response.body().string());

      return response.body().string();
    } catch (IOException e) {
      LOG.error("Querying 'admin_nodInfo failed", e);
      throw new RuntimeException(e);
    }
  }

  private WebClient jsonRpcWebClient() {
    if (jsonRpc == null) {
      // TODO move the vertx to network & close on stop()
      jsonRpc =
          WebClient.create(
              Vertx.vertx(),
              new WebClientOptions()
                  .setDefaultPort(besu.getMappedPort(CONTAINER_HTTP_RPC_PORT))
                  .setDefaultHost(besu.getContainerIpAddress()));
    }

    return jsonRpc;
  }

  // TODO reduce the args - exposed ports maybe not needed
  private GenericContainer<?> besuContainer(final NodeConfiguration config) {
    return new GenericContainer<>(BESU_IMAGE)
        .withNetwork(config.getContainerNetwork().orElse(null))
        .withExposedPorts(CONTAINER_HTTP_RPC_PORT, CONTAINER_WS_RPC_PORT, CONTAINER_P2P_PORT)
        .withFileSystemBind(config.getGenesisFile(), CONTAINER_GENESIS_FILE, BindMode.READ_ONLY)
        .withFileSystemBind(
            config.getEnclavePublicKeyFile(),
            CONTAINER_PRIVACY_PUBLIC_KEY_FILE,
            BindMode.READ_ONLY);
  }

  // TODO liveliness should be move to network or configurable to allow parallel besu container
  // startups
  private HttpWaitStrategy liveliness() {
    return Wait.forHttp(AM_I_ALIVE_ENDPOINT)
        .forStatusCode(ALIVE_STATUS_CODE)
        .forPort(CONTAINER_HTTP_RPC_PORT);
  }

  // TODO a single log line with all details!
  private void logHttpRpcPortMapping() {
    LOG.info(
        "Container {}, HTTP RPC port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_HTTP_RPC_PORT,
        besu.getMappedPort(CONTAINER_HTTP_RPC_PORT));
  }

  private void logWsRpcPortMapping() {
    LOG.info(
        "Container {}, WS RPC port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_WS_RPC_PORT,
        besu.getMappedPort(CONTAINER_WS_RPC_PORT));
  }

  private void logPeerToPeerPortMapping() {
    LOG.info(
        "Container {}, p2p port mapping: {} -> {}",
        besu.getContainerId(),
        CONTAINER_P2P_PORT,
        besu.getMappedPort(CONTAINER_P2P_PORT));
  }

  private void logContainerNetworkDetails() {
    if (besu.getNetwork() == null) {
      LOG.info("Container {} has no network", besu.getContainerId());
    } else {
      LOG.info(
          "Container {}, IP address: {}, Network: {}",
          besu.getContainerId(),
          besu.getContainerIpAddress(),
          besu.getNetwork().getId());
    }
  }
}
