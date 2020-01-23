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
package tech.pegasys.peeps.network;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import tech.pegasys.peeps.node.Besu;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkTest {

  @Mock private Besu node;

  @BeforeEach
  public void setUp() {

    // lenient()
    // lenient().when(node.identity()).thenReturn(NodeKey.ALPHA);
  }

  @SuppressWarnings("resource")
  @Test
  public void startMustStartNodes(@TempDir Path configurationDirectory) {

    final Network network = new Network(configurationDirectory);

    network.addNode(node);

    verify(node).start();
    verifyNoMoreInteractions(node);
  }
}
