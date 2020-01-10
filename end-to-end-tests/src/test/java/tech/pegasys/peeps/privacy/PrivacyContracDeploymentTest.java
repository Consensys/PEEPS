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
import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.signer.SignerKeys;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private Network network;
  private Besu besuA;
  private Orion orionA;
  private EthSigner signerA;

  private Besu besuB;
  private EthSigner signerB;
  private Orion orionB;

  @Override
  protected void setUpNetwork(final Network network) {
    this.network = network;

    this.orionA =
        network.addPrivacyManager(
            new OrionConfigurationBuilder()
                .withPrivateKeys(OrionKeys.ONE.getPrivateKey())
                .withPublicKeys(OrionKeys.ONE.getPublicKey()));

    // TODO Besu -> Orion
    this.besuA =
        network.addNode(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(orionA.getNetworkRpcAddress())
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .withPrivacyManagerPublicKey(OrionKeys.ONE.getPublicKey()));

    this.signerA =
        network.addSigner(
            new EthSignerConfigurationBuilder()
                .withChainId(besuA.chainId())
                .withKeyFile(SignerKeys.WALLET_A.getKeyResource())
                .withPasswordFile(SignerKeys.WALLET_A.getPasswordResource()),
            besuA);

    // TODO More typing then a String - URI, URL, File or Path
    final List<String> orionBootnodes = new ArrayList<>();
    orionBootnodes.add(orionA.getPeerNetworkAddress());

    this.orionB =
        network.addPrivacyManager(
            new OrionConfigurationBuilder()
                .withPrivateKeys(OrionKeys.TWO.getPrivateKey())
                .withPublicKeys(OrionKeys.TWO.getPublicKey())
                .withBootnodeUrls(orionBootnodes));

    // TODO fits as a function of Besu
    // TODO better typing then String - create ENODE Address
    final String bootnodeEnodeAddress =
        NodeKeys.BOOTNODE.getEnodeAddress(besuA.ipAddress(), besuA.p2pPort());

    this.besuB =
        network.addNode(
            new BesuConfigurationBuilder()
                .withPrivacyUrl(orionB.getNetworkRpcAddress())
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .withPrivacyManagerPublicKey(OrionKeys.TWO.getPublicKey()));

    this.signerB =
        network.addSigner(
            new EthSignerConfigurationBuilder()
                .withChainId(besuB.chainId())
                .withKeyFile(SignerKeys.WALLET_B.getKeyResource())
                .withPasswordFile(SignerKeys.WALLET_B.getPasswordResource()),
            besuB);
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    final Hash pmt =
        signerA.rpc().deployContractToPrivacyGroup(SimpleStorage.BINARY, orionA, orionB);

    // TODO no in-line comments - implement clean code!

    // TODO entire network - i.e. all Nodes, don't pass them in
    // Valid transaction receipt for the privacy contract deployment
    network.awaitConsensusOn(pmt, besuA, besuB);

    // Valid privacy marker transaction
    final TransactionReceipt pmtReceiptNodeA = besuA.rpc().getTransactionReceipt(pmt);

    assertThat(pmtReceiptNodeA.getTransactionHash()).isEqualTo(pmt);
    assertThat(pmtReceiptNodeA.isSuccess()).isTrue();

    final Transaction pmtNodeA = besuA.rpc().getTransactionByHash(pmt);
    final Transaction pmtNodeB = besuB.rpc().getTransactionByHash(pmt);

    assertThat(pmtNodeA.isProcessed()).isTrue();
    assertThat(pmtNodeA).usingRecursiveComparison().isEqualTo(pmtNodeB);
    assertThat(pmtNodeA).isNotNull();

    // Convert from Hex String to Base64 UTF_8 String for Orion
    final byte[] decodedHex = Hex.decodeHex(removeAnyHexPrefix(pmtNodeA.getInput()).toCharArray());
    final byte[] encodedHexB64 = Base64.encodeBase64(decodedHex);
    final String key = new String(encodedHexB64, StandardCharsets.UTF_8);

    // Valid privacy transaction receipt
    final PrivacyTransactionReceipt receiptNodeA = besuA.rpc().getPrivacyContractReceipt(pmt);
    final PrivacyTransactionReceipt receiptNodeB = besuB.rpc().getPrivacyContractReceipt(pmt);

    assertThat(receiptNodeA.isSuccess()).isTrue();
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeB);

    final PrivacyTransactionReceipt receiptNodeC = signerB.rpc().getPrivacyContractReceipt(pmt);
    assertThat(receiptNodeA).usingRecursiveComparison().isEqualTo(receiptNodeC);

    // Valid entries in both Orions
    final String payloadOrionA = orionA.getPayload(key);
    final String payloadOrionB = orionB.getPayload(key);

    assertThat(payloadOrionA).isNotNull();
    assertThat(payloadOrionA).isEqualTo(payloadOrionB);
  }
}
