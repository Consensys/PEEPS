/*
 * Copyright 2020 ConsenSys AG.
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
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.rpc.NodeRpcExpectingData;
import tech.pegasys.peeps.privacy.Orion;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignerRpcExpectingData extends NodeRpcExpectingData {

  private static final Logger LOG = LogManager.getLogger();

  // TODO better typing
  // TODO enter / stored in the wallet file as address - can be read in EthSigner
  private final String senderAccount = "0xf17f52151ebef6c7334fad080c5704d77216b732";
  //  private final String senderAccount = "0x627306090abab3a6e1400e9345bc60c78a8bef57";

  private final SignerRpc rpc;
  final Supplier<String> signerLogs;
  private final Supplier<String> downstreamLogs;

  public SignerRpcExpectingData(
      final SignerRpc rpc,
      final Supplier<String> signerLogs,
      final Supplier<String> downstreamLogs) {
    super(rpc);
    this.rpc = rpc;
    this.downstreamLogs = downstreamLogs;
    this.signerLogs = signerLogs;
  }

  public Hash deployContractToPrivacyGroup(
      final String binary, final Orion sender, final Orion... recipients) {
    final String[] privateRecipients = new String[recipients.length];
    for (int i = 0; i < recipients.length; i++) {
      privateRecipients[i] = recipients[i].getId();
    }

    try {
      return rpc.deployContractToPrivacyGroup(
          senderAccount, binary, sender.getId(), privateRecipients);
    } catch (final RuntimeException e) {
      LOG.error(signerLogs.get());
      LOG.error(downstreamLogs.get());
      throw e;
    }
  }
}
