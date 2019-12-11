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
package tech.pegasys.peeps.node.rpc.priv;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivacyTransactionReceipt {

  private final String from;
  private final String to;
  private final String gas;
  private final String gasPrice;
  private final String hash;
  private final String input;
  private final String nonce;
  private final String value;
  private final String v;
  private final String r;
  private final String s;
  private final String privateFrom;
  private final String[] privateFor;
  private final String restriction;

  // TODO better typing than String

  public PrivacyTransactionReceipt(
      @JsonProperty("from") final String from,
      @JsonProperty("to") final String to,
      @JsonProperty("gas") final String gas,
      @JsonProperty("gasPrice") final String gasPrice,
      @JsonProperty("hash") final String hash,
      @JsonProperty("input") final String input,
      @JsonProperty("nonce") final String nonce,
      @JsonProperty("value") final String value,
      @JsonProperty("v") final String v,
      @JsonProperty("r") final String r,
      @JsonProperty("s") final String s,
      @JsonProperty("privateFrom") final String privateFrom,
      @JsonProperty("privateFor") final String[] privateFor,
      @JsonProperty("restriction") final String restriction) {
    this.from = from;
    this.to = to;
    this.gas = gas;
    this.gasPrice = gasPrice;
    this.hash = hash;
    this.input = input;
    this.nonce = nonce;
    this.value = value;
    this.v = v;
    this.r = r;
    this.s = s;
    this.privateFrom = privateFrom;
    this.privateFor = privateFor;
    this.restriction = restriction;
  }

  public String getFrom() {
    return from;
  }

  public Optional<String> getTo() {
    return Optional.ofNullable(to);
  }

  public String getGas() {
    return gas;
  }

  public String getGasPrice() {
    return gasPrice;
  }

  public String getHash() {
    return hash;
  }

  public String getInput() {
    return input;
  }

  public String getNonce() {
    return nonce;
  }

  public String getValue() {
    return value;
  }

  public String getV() {
    return v;
  }

  public String getR() {
    return r;
  }

  public String getS() {
    return s;
  }

  public String getPrivateFrom() {
    return privateFrom;
  }

  public String[] getPrivateFor() {
    return privateFor;
  }

  public String getRestriction() {
    return restriction;
  }
}
