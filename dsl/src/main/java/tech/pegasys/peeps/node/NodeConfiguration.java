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
package tech.pegasys.peeps.node;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeConfiguration {

  private static final Logger LOG = LogManager.getLogger();

  private final String genesisFilePath;
  private final Optional<String> cors;

  public NodeConfiguration(final String genesisFilePath, final String cors) {

    // TODO move this elsewhere
    final URL resource =
        Thread.currentThread().getContextClassLoader().getResource(genesisFilePath);

    //    final URL resource = Resources.getResource(genesisFilePath);

    if (resource == null) {
      final String message = String.format("File '%s' not found on classpath", genesisFilePath);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    try {
      this.genesisFilePath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8.name());
    } catch (final UnsupportedEncodingException ex) {
      LOG.error("Unsupported encoding used to decode {}, filepath.", resource);
      throw new RuntimeException("Illegal string decoding");
    }

    this.cors = Optional.ofNullable(cors);
  }

  public String getGenesisFilePath() {
    return genesisFilePath;
  }

  public Optional<String> getCors() {
    return cors;
  }
}
