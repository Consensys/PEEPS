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
package tech.pegasys.peeps.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import tech.pegasys.peeps.privacy.model.PrivacyAddreess;

import org.junit.jupiter.api.Test;

public class PrivacyAddressTest {

  @Test
  public void missingIdMustException() {
    final Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new PrivacyAddreess(null);
            });

    assertThat(exception.getMessage()).isEqualTo("Address is mandatory");
  }

  @Test
  public void hashCodeMustMatchIdHashCode() {
    final String id = "A very unique address";

    final PrivacyAddreess nodeId = new PrivacyAddreess(id);

    assertThat(nodeId.hashCode()).isEqualTo(id.hashCode());
  }

  @Test
  public void selfReferenceEqualityMustSucced() {
    final PrivacyAddreess nodeId = new PrivacyAddreess("I am a real address!");

    final boolean isEquals = nodeId.equals(nodeId);

    assertThat(isEquals).isTrue();
  }

  @Test
  public void identicalIdEqualityMustSucced() {
    final String id = "A not so unique address";
    final PrivacyAddreess nodeIdAlpha = new PrivacyAddreess(id);
    final PrivacyAddreess nodeIdBeta = new PrivacyAddreess(id);

    final boolean isEquals = nodeIdAlpha.equals(nodeIdBeta);

    assertThat(isEquals).isTrue();
  }

  @Test
  public void noReferenceEqualityMustFail() {
    final PrivacyAddreess nodeId = new PrivacyAddreess("I am a real address value!");

    final boolean isEquals = nodeId.equals(null);

    assertThat(isEquals).isFalse();
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void differentTypeEqualityMustFail() {
    final PrivacyAddreess nodeId = new PrivacyAddreess("I am a real address value!");

    final boolean isEquals = nodeId.equals("Type of String");

    assertThat(isEquals).isFalse();
  }

  @Test
  public void differentIdEqualityMustFail() {
    final PrivacyAddreess nodeIdAlpha = new PrivacyAddreess("First address");
    final PrivacyAddreess nodeIdBeta = new PrivacyAddreess("Second address");

    final boolean isEquals = nodeIdAlpha.equals(nodeIdBeta);

    assertThat(isEquals).isFalse();
  }

  @Test
  public void hashCodeMustEqualIdHashCode() {
    final String id = "The one and only address!";
    final PrivacyAddreess nodeId = new PrivacyAddreess(id);

    final int expected = id.hashCode();
    final int actual = nodeId.hashCode();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getMustReturnAddress() {
    final String id = "The address!";
    final PrivacyAddreess nodeId = new PrivacyAddreess(id);

    final String actual = nodeId.get();

    assertThat(actual).isEqualTo(id);
  }
}
