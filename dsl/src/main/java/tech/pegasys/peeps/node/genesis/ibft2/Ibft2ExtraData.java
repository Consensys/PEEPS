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
package tech.pegasys.peeps.node.genesis.ibft2;

import tech.pegasys.peeps.node.Besu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.Hash;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.rlp.RLP;

public class Ibft2ExtraData {

  public static Bytes encode(final Besu... validators) {

    // TODO get the node public key from Besu
    Stream.of(validators)
        .parallel()
        .map(
            validator -> {
              final Bytes32 a = Hash.keccak256(Bytes.fromHexStringLenient(validator.nodeKey()));

              return null;
            })
        .collect(Collectors.toList());

    return null;
  }

  public static Bytes encode(final List<Address> validators) {
    final byte[] vanityData = new byte[32];
    final int round = 0;
    final byte[] votes = new byte[0];

    return RLP.encode(
        writer -> {
          writer.writeByteArray(vanityData);
          writer.writeList(validators, (rlp, validator) -> rlp.writeValue(validator.toBytes()));
          writer.writeByteArray(votes);
          writer.writeInt(round);
        });

    /*
        final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
       encoder.startList();
       encoder.writeBytes(vanityData);
       encoder.writeList(validators, (validator, rlp) -> rlp.writeBytes(validator));
       if (vote.isPresent()) {
         vote.get().writeTo(encoder);
       } else {
         encoder.writeNull();
       }

       if (encodingType != EncodingType.EXCLUDE_COMMIT_SEALS_AND_ROUND_NUMBER) {
         encoder.writeInt(round);
         if (encodingType != EncodingType.EXCLUDE_COMMIT_SEALS) {
           encoder.writeList(seals, (committer, rlp) -> rlp.writeBytes(committer.encodedBytes()));
         }
       }
       encoder.endList();
    */

  }
}
