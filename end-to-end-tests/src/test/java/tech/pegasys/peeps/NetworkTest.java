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

import tech.pegasys.peeps.network.Network;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class NetworkTest {

  @TempDir Path configurationDirectory;

  // TODO encapsulate this better - hide from the subclasses
  protected Network network;

  @BeforeEach
  public void setUp() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
    network = new Network(configurationDirectory);
    network.start();
  }

  @AfterEach
  public void tearDown() {
    network.close();
  }
}
