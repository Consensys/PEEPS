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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.util.Await;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;

public class Network implements Closeable {

  // TODO cater for one-many & many-one for Besu/Orion
  // TODO cater for one-many for Besu/EthSigner

  private final List<NetworkMember> members;
  private final List<Besu> nodes;
  private final List<EthSigner> signers;
  private final List<Orion> privacyManagers;

  // TODO relationship mappings

  private final Subnet subnet;
  private final org.testcontainers.containers.Network network;
  private final PathGenerator pathGenerator;
  private final Vertx vertx;

  public Network(final Path configurationDirectory) {
    checkNotNull(configurationDirectory, "Path to configuration directory is mandatory");

    this.privacyManagers = new ArrayList<>();
    this.members = new ArrayList<>();
    this.signers = new ArrayList<>();
    this.nodes = new ArrayList<>();

    this.pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();

    this.subnet = new Subnet();
    this.network = subnet.createContainerNetwork();
  }

  public void start() {
    members.parallelStream().forEach(member -> member.start());

    awaitConnectivity();
  }

  public void stop() {
    members.parallelStream().forEach(member -> member.stop());
  }

  @Override
  public void close() {
    stop();
    vertx.close();
    network.close();
  }

  private void awaitConnectivity() {

    nodes.parallelStream().forEach(node -> node.awaitConnectivity(nodes));
    privacyManagers
        .parallelStream()
        .forEach(privacyManger -> privacyManger.awaitConnectivity(privacyManagers));

    // TODO code : need relationship between signers & besus
    //
    // signerA.awaitConnectivity(besuA);
    // signerB.awaitConnectivity(besuB);
  }

  public Besu addNode(final BesuConfigurationBuilder config) {
    final Besu besu =
        new Besu(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .build());

    nodes.add(besu);
    members.add(besu);

    return besu;
  }

  public Orion addPrivacyManager(final OrionConfigurationBuilder config) {

    final Orion manager =
        new Orion(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
                .withBootnodeUrls(privacyManagerBootnodeUrls())
                .build());

    privacyManagers.add(manager);
    members.add(manager);

    return manager;
  }

  private List<String> privacyManagerBootnodeUrls() {
    return privacyManagers
        .parallelStream()
        .map(manager -> manager.getPeerNetworkAddress())
        .collect(Collectors.toList());
  }

  public EthSigner addSigner(final EthSignerConfigurationBuilder config, final Besu downstream) {
    final EthSigner signer =
        new EthSigner(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .withDownstreamHost(downstream.ipAddress())
                .withDownstreamPort(downstream.httpRpcPort())
                .build());

    signers.add(signer);
    members.add(signer);

    return signer;
  }

  // TODO restructure, maybe Supplier related or a utility on network?
  // TODO stricter typing than String
  public void awaitConsensusOn(final Hash receiptHash, final Besu besuA, final Besu besuB) {
    Await.await(
        () -> {
          final TransactionReceipt pmtReceiptNodeA = besuA.rpc().getTransactionReceipt(receiptHash);
          final TransactionReceipt pmtReceiptNodeB = besuB.rpc().getTransactionReceipt(receiptHash);

          assertThat(pmtReceiptNodeA).isNotNull();
          assertThat(pmtReceiptNodeA.isSuccess()).isTrue();
          assertThat(pmtReceiptNodeA).usingRecursiveComparison().isEqualTo(pmtReceiptNodeB);
        },
        "Consensus was not reached in time for receipt hash: " + receiptHash);
  }
}
