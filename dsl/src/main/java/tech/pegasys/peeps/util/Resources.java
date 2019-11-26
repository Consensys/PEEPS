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
package tech.pegasys.peeps.util;

import static tech.pegasys.peeps.util.HexFormatter.removeAnyHexPrefix;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resources {

  private static final Logger LOG = LogManager.getLogger();

  public static String getCanonicalPath(final String path) {
    final URL resource = getResource(path);

    try {
      return URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8.name());
    } catch (final UnsupportedEncodingException e) {
      LOG.error("Unsupported encoding found when decoding: {}.", resource);
      throw new RuntimeException(e);
    }
  }

  public static String readHexDroppingPrefix(final String path) {
    final URL resource = getResource(path);

    try {
      return removeAnyHexPrefix(
          Files.readString(Path.of(resource.toURI()), StandardCharsets.UTF_8));
    } catch (final IOException | URISyntaxException e) {
      throw new RuntimeException("Problem reading file: " + path, e);
    }
  }

  private static URL getResource(final String path) {
    final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

    if (resource == null) {
      final String message = String.format("File '%s' not found on classpath", path);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    return resource;
  }
}
