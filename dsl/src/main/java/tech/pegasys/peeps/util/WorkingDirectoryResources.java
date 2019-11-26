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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.common.base.Splitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkingDirectoryResources {

  private static final Logger LOG = LogManager.getLogger();

  public static String getCanonicalPath(final String path) {
    return decodePath(existentResourceUrl(path));
  }

  public static String readHexDroppingAnyPrefix(final String path) {
    return removeAnyHexPrefix(readString(existentResourceUrl(path)));
  }

  public static String readHexDroppingAnyPrefixIs(final String path) {
    final InputStream resource =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

    if (resource == null) {
      final String message = String.format("File '%s' is not found on classpath", path);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    String text;
    try (Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8.name())) {
      text = scanner.useDelimiter("\\A").next();
    }

    return removeAnyHexPrefix(text);
  }

  public static String readHexDroppingAnyPrefixAlt(final String path) {
    try {
      return removeAnyHexPrefix(
          com.google.common.io.Resources.toString(
              com.google.common.io.Resources.getResource(path), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Failed to init FileSystem to access resource: " + path, e);
    }
  }

  private static URL existentResourceUrl(final String path) {

    final File resource = new File(path);

    if (!resource.exists()) {
      final String message;
      try {
        message = String.format("File '%s' is not found", resource.getCanonicalPath());
        LOG.error(message);
      } catch (IOException e) {

      }
      throw new IllegalArgumentException();
    }

    try {
      return resource.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static URL existentResourceUrlAlt(final String path) {
    final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

    if (resource == null) {
      final String message = String.format("File '%s' is not found on classpath", path);
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    return resource;
  }

  private static String decodePath(final URL resource) {
    return URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
  }

  public static String readStringAlt(final URL resource) {
    final URI resourceUri;

    try {

      resourceUri = resource.toURI();

    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }

    try {
      return readString(resourceUri);
    } catch (final FileSystemNotFoundException e) {

      try {
        initFileSystem(resourceUri);
        return readString(resourceUri);
      } catch (final IOException ex) {
        throw new IllegalArgumentException(
            "Failed to init FileSystem to access resource: " + resourceUri, ex);
      }
    }
  }

  private static void initFileSystem(final URI resource) throws IOException {
    LOG.info("Resource: {}", resource);
    LOG.info("Path: {}", resource.getPath());
    LOG.info("Fragment: {}", resource.getFragment());
    LOG.info("Query: {}", resource.getQuery());

    final List<String> array = Splitter.on('!').splitToList(resource.toString());
    final Map<String, String> env = new HashMap<>();
    env.put("create", "true");

    LOG.info("Index 0: {}", array.get(0));

    FileSystems.newFileSystem(URI.create(array.get(0)), env);
  }

  private static String readString(final URL resource) {
    try {
      return readString(resource.toURI());
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Cannot read file: " + resource, e);
    }
  }

  private static String readString(final URI resource) {
    try {
      return Files.readString(Path.of(resource), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + resource, e);
    }
  }
}
