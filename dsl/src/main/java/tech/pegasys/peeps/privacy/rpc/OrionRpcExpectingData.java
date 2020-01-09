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
package tech.pegasys.peeps.privacy.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.util.Await;

import java.util.function.Supplier;

public class OrionRpcExpectingData {

  private final OrionRpc rpc;

  public OrionRpcExpectingData(final OrionRpc rpc) {
    this.rpc = rpc;
  }

  public String send(final String to, final String payload) {
    return awaitRpc(
        () -> rpc.send(to, payload), "Failed to sent payload: %s, to peer: %s", payload, to);
  }

  public String receive(final String receipt) {
    return awaitRpc(() -> rpc.receive(receipt), "Failed to retrieve payload with key: %s", receipt);
  }

  private String awaitRpc(
      final Supplier<String> rpc,
      final String errorMessage,
      final Object... errorMessageParameters) {

    Await.await(
        () -> {
          assertThat(rpc.get()).isNotBlank();
        },
        String.format(errorMessage, errorMessageParameters));

    return rpc.get();
  }
}
