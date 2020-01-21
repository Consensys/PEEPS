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

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.NodeKey;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.privacy.model.OrionKey;
import tech.pegasys.peeps.signer.SignerWallet;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private final NodeKey nodeAlpha = NodeKey.ALPHA;
  private final SignerWallet signerAlpha = SignerWallet.ALPHA;

  private Orion privacyManagerAlpha;
  private Orion privacyManagerBeta;

  @Override
  protected void setUpNetwork(final Network network) {

    this.privacyManagerAlpha = network.addPrivacyManager(OrionKeyPair.ALPHA);

    // TODO Besu -> Orion
    // TODO Orion can expose it's public keys, choose the first
    network.addNode(
        new BesuConfigurationBuilder()
            .withPrivacyUrl(privacyManagerAlpha)
            .withIdentity(NodeKey.ALPHA)
            .withPrivacyManagerPublicKey(OrionKeyPair.ALPHA.getPublicKey()));

    network.addSigner(SignerWallet.ALPHA, nodeAlpha);

    this.privacyManagerBeta = network.addPrivacyManager(OrionKeyPair.BETA);

    network.addNode(
        new BesuConfigurationBuilder()
            .withPrivacyUrl(privacyManagerBeta)
            .withIdentity(NodeKey.BETA)
            .withPrivacyManagerPublicKey(OrionKeyPair.BETA.getPublicKey()));
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    final Hash pmt =
        execute(signerAlpha)
            .deployContractToPrivacyGroup(
                signerAlpha.address(),
                SimpleStorage.BINARY,
                privacyManagerAlpha,
                privacyManagerBeta);

    await().consensusOnTransactionReciept(pmt);

    verify(nodeAlpha).successfulTransactionReceipt(pmt);
    verify().consensusOnTransaction(pmt);
    verify().consensusOnPrivacyTransactionReceipt(pmt);

    // TODO no in-line comments - implement clean code!

    // Valid entries in both Orions
    final Transaction pmtNodeA = execute(nodeAlpha).getTransactionByHash(pmt);
    final OrionKey key = OrionKey.from(pmtNodeA);

    final String payloadOrionA = privacyManagerAlpha.getPayload(key);
    final String payloadOrionB = privacyManagerBeta.getPayload(key);

    assertThat(payloadOrionA).isNotNull();
    assertThat(payloadOrionA).isEqualTo(payloadOrionB);
  }
}
