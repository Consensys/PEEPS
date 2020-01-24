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

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.NodeKeys;
import tech.pegasys.peeps.OrionKeyPair;
import tech.pegasys.peeps.SignerWallet;
import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.privacy.model.PrivacyManagerIdentifier;
import tech.pegasys.peeps.signer.model.SignerIdentifier;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

public class PrivacyContracDeploymentTest extends NetworkTest {

  private final NodeIdentifier nodeAlpha = new NodeIdentifier(NodeKeys.ALPHA.name());
  private final SignerIdentifier signerAlpha = new SignerIdentifier(SignerWallet.ALPHA.name());
  private final PrivacyManagerIdentifier privacyManagerAlpha =
      new PrivacyManagerIdentifier(OrionKeyPair.ALPHA.name());
  private final PrivacyManagerIdentifier privacyManagerBeta =
      new PrivacyManagerIdentifier(OrionKeyPair.BETA.name());

  @Override
  protected void setUpNetwork(final Network network) {
    network.addPrivacyManager(privacyManagerAlpha, OrionKeyPair.ALPHA.getKeyPair());
    network.addPrivacyManager(privacyManagerBeta, OrionKeyPair.BETA.getKeyPair());
    network.addNode(
        nodeAlpha,
        NodeKeys.ALPHA.keys(),
        privacyManagerAlpha,
        OrionKeyPair.ALPHA.getKeyPair().getPublicKey());
    network.addNode(
        new NodeIdentifier(NodeKeys.BETA.name()),
        NodeKeys.BETA.keys(),
        privacyManagerBeta,
        OrionKeyPair.BETA.getKeyPair().getPublicKey());
    network.addSigner(signerAlpha, SignerWallet.ALPHA.resources(), nodeAlpha);
  }

  @Test
  public void deploymentMustSucceed() throws DecoderException {

    final Hash pmt =
        execute(signerAlpha)
            .deployContractToPrivacyGroup(
                SignerWallet.ALPHA.address(),
                SimpleStorage.BINARY,
                OrionKeyPair.ALPHA.getAddress(),
                OrionKeyPair.BETA.getAddress());

    await().consensusOnTransactionReciept(pmt);

    verify(nodeAlpha).successfulTransactionReceipt(pmt);
    verify().consensusOnTransaction(pmt);
    verify().consensusOnPrivacyTransactionReceipt(pmt);
    verify()
        .privacyGroup(privacyManagerAlpha, privacyManagerBeta)
        .consensusOnPrivacyPayload(execute(nodeAlpha).getTransactionByHash(pmt));
  }
}
