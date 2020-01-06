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
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.privacy.OrionKeys;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.util.Await;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;

public class Network implements Closeable {

  // TODO do not be hard coded as two nodes - flexibility in nodes & stack
  // TODO cater for one-many & many-one for Besu/Orion
  // TODO cater for one-many for Besu/EthSigner

  private final List<NetworkMember> members;

  private final List<Besu> nodes;
  private final List<EthSigner> signers;
  private final List<Orion> privacyTransactionManagers;

  // TODO relationship mappings

  private final Besu besuA;
  private final Orion orionA;
  private final EthSigner signerA;

  private final Besu besuB;
  private final EthSigner signerB;
  private final Orion orionB;

  private final Subnet subnet;
  private final org.testcontainers.containers.Network network;
  private final PathGenerator pathGenerator;

  private final Vertx vertx;

  // TODO choosing the topology should be elsewhere
  public Network(final Path configurationDirectory) {
    checkNotNull(configurationDirectory, "Path to configuration directory is mandatory");

    this.privacyTransactionManagers = new ArrayList<>();
    this.members = new ArrayList<>();
    this.signers = new ArrayList<>();
    this.nodes = new ArrayList<>();

    this.pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();

    this.subnet = new Subnet();
    this.network = subnet.createContainerNetwork();

    // TODO name files according the account pubkey

    // TODO these should come from somewhere, programmatically generated?
    final String keyFileSignerA = "signer/account/funded/wallet_a.v3";
    final String passwordFileSignerA = "signer/account/funded/wallet_a.pass";
    final String keyFileSignerB = "signer/account/funded/wallet_b.v3";
    final String passwordFileSignerB = "signer/account/funded/wallet_b.pass";

    this.orionA =
        privacyTransactionManager(
            new OrionConfigurationBuilder()
                .withPrivateKeys(OrionKeys.ONE.getPrivateKey())
                .withPublicKeys(OrionKeys.ONE.getPublicKey()));

    this.besuA =
        node(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(orionA.getNetworkRpcAddress())
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .withPrivacyManagerPublicKey(OrionKeys.ONE.getPublicKey()));

    this.signerA =
        signer(
            new EthSignerConfigurationBuilder()
                .withChainId(besuA.chainId())
                .withKeyFile(keyFileSignerA)
                .withPasswordFile(passwordFileSignerA),
            besuA);

    // TODO More typing then a String - URI, URL, File or Path
    final List<String> orionBootnodes = new ArrayList<>();
    orionBootnodes.add(orionA.getPeerNetworkAddress());

    this.orionB =
        privacyTransactionManager(
            new OrionConfigurationBuilder()
                .withPrivateKeys(OrionKeys.TWO.getPrivateKey())
                .withPublicKeys(OrionKeys.TWO.getPublicKey())
                .withBootnodeUrls(orionBootnodes));

    // TODO better typing then String
    final String bootnodeEnodeAddress =
        NodeKeys.BOOTNODE.getEnodeAddress(besuA.ipAddress(), besuA.p2pPort());

    this.besuB =
        node(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(orionB.getNetworkRpcAddress())
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .withPrivacyManagerPublicKey(OrionKeys.TWO.getPublicKey()));

    this.signerB =
        signer(
            new EthSignerConfigurationBuilder()
                .withChainId(besuB.chainId())
                .withKeyFile(keyFileSignerB)
                .withPasswordFile(passwordFileSignerB),
            besuB);
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
    besuA.awaitConnectivity(besuB);
    besuB.awaitConnectivity(besuA);
    orionA.awaitConnectivity(orionB);
    orionB.awaitConnectivity(orionA);

    signerA.awaitConnectivity(besuA);
    signerB.awaitConnectivity(besuB);
  }

  private Besu node(final BesuConfigurationBuilder config) {

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

  private Orion privacyTransactionManager(final OrionConfigurationBuilder config) {

    final Orion manager =
        new Orion(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
                .build());

    privacyTransactionManagers.add(manager);
    members.add(manager);

    return manager;
  }

  private EthSigner signer(final EthSignerConfigurationBuilder config, final Besu downstream) {
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
  public void awaitConsensusOn(final String receiptHash) {

    Await.await(
        () -> {
          final TransactionReceipt pmtReceiptNodeA = besuA.getTransactionReceipt(receiptHash);
          final TransactionReceipt pmtReceiptNodeB = besuB.getTransactionReceipt(receiptHash);

          assertThat(pmtReceiptNodeA).isNotNull();
          assertThat(pmtReceiptNodeA.isSuccess()).isTrue();
          assertThat(pmtReceiptNodeA).usingRecursiveComparison().isEqualTo(pmtReceiptNodeB);
        },
        "Consensus was not reached in time for receipt hash: " + receiptHash);
  }

  // TODO interfaces for the signer used by the test?
  // TODO figure out a nicer way for the UT to get a handle on the signers
  public EthSigner getSignerA() {
    return signerA;
  }

  public EthSigner getSignerB() {
    return signerB;
  }

  // TODO figure out a nicer way for the UT to get a handle on the node or send
  // requests
  public Besu getNodeA() {
    return besuA;
  }

  // TODO figure out a nicer way for the UT to get a handle on the node or send
  // requests
  public Besu getNodeB() {
    return besuB;
  }

  // TODO figure out a nicer way for the UT to get a handle on the Orion or send
  // requests
  public Orion getOrionA() {
    return orionA;
  }

  // TODO figure out a nicer way for the UT to get a handle on the Orion or send
  // requests
  public Orion getOrionB() {
    return orionB;
  }

  // TODO provide a handle for Besus too? (web3j?)
}
