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
package tech.pegasys.peeps;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.contract.SimpleStorage;
import tech.pegasys.peeps.node.rpc.priv.PrivacyTransactionReceipt;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO extract common network setup into superclass
public class PrivacyContractDeployment {

  private final Network network = new Network();

  @BeforeEach
  public void startUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    network.start();
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }

  @Test
  public void a() {
    final String receiptHash = network.getSignerA().deployContract(SimpleStorage.BINARY);

    // TODO get contract deployment receipt from NodeA
    final Optional<PrivacyTransactionReceipt> receiptNodeA =
        network.getNodeA().getPrivacyTransactionReceipt(receiptHash);

    // TODO get contract deployment receipt from NodeB
    final Optional<PrivacyTransactionReceipt> receiptNodeB =
        network.getNodeA().getPrivacyTransactionReceipt(receiptHash);

    // TODO verify receipt is valid, contains a contract address
    assertThat(receiptNodeA).isNotNull();
    assertThat(receiptNodeB).isNotNull();

    // TODO verify the state of the Orions & state of each Besu - side effects
  }
}
