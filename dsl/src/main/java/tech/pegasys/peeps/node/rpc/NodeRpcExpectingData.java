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
package tech.pegasys.peeps.node.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.node.model.PrivacyTransactionReceipt;
import tech.pegasys.peeps.node.model.Transaction;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.util.Await;

import java.util.Optional;
import java.util.function.Supplier;

public class NodeRpcExpectingData {

  private final NodeRpc rpc;

  public NodeRpcExpectingData(final NodeRpc rpc) {
    this.rpc = rpc;
  }

  public PrivacyTransactionReceipt getPrivacyContractReceipt(final String receiptHash) {
    return awaitRpc(
        () -> rpc.getPrivacyTransactionReceipt(receiptHash),
        "Failed to retrieve the private transaction receipt with hash: %s",
        receiptHash);
  }

  public TransactionReceipt getTransactionReceipt(final String receiptHash) {
    return awaitRpc(
        () -> rpc.getTransactionReceipt(receiptHash),
        "Failed to retrieve the transaction receipt with hash: %s",
        receiptHash);
  }

  public Transaction getTransactionByHash(final String hash) {
    return awaitRpc(
        () -> rpc.getTransactionByHash(hash),
        "Failed to retrieve the transaction with hash: %s",
        hash);
  }

  private <T> T awaitRpc(
      final Supplier<Optional<T>> rpc,
      final String errorMessage,
      final Object... errorMessageParameters) {

    Await.await(
        () -> {
          assertThat(rpc.get()).isPresent();
        },
        String.format(errorMessage, errorMessageParameters));

    return rpc.get().get();
  }
}
