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
package tech.pegasys.peeps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;

public class SubnetworkTest {

  private final List<Network> cleanUp = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    cleanUp.clear();
  }

  @AfterEach
  public void tearDown() {
    cleanUp.stream().forEach(network -> network.close());
  }

  @Test
  public void canCreateOneNetwork() {
    final Network one = new Subnetwork().build();

    start(one);
  }

  @Test
  public void canCreateTwoConcurrentlyLiveNetworks() {
    final Network first = new Subnetwork().build();
    final Network second = new Subnetwork().build();

    start(first);
    start(second);
  }

  @Test
  public void canCreateTwoSequentallyLiveNetworks() {
    final Network first = new Subnetwork().build();
    start(first);

    final Network second = new Subnetwork().build();
    start(second);
  }

  /**
   * TestContainers uses lazy creation for the Docker network client calls.
   *
   * <p>Wraps up the calls and manages clean up of the Docker networks.
   */
  private void start(final Network network) {
    assertThat(network.getId()).isNotEmpty();

    cleanUp.add(network);
  }
}
