package tech.pegasys.peeps.privacy;

//TODO maybe not use an enum?
public enum OrionKeys {

  ONE("privacy/keys/key_0"),
  TWO("privacy/keys/key_1"),
  THREE("privacy/keys/key_2"),
  FOUR("privacy/keys/key_3");

  private static final String PRIVATE_KEY_FILENAME = "%s.priv";
  private static final String PUBLIC_KEY_FILENAME = "%s.pub";

  private final String pubKey;
  private final String privKey;

  OrionKeys(final String name) {
    privKey = String.format(PRIVATE_KEY_FILENAME, name);
    pubKey = String.format(PUBLIC_KEY_FILENAME, name);
  }

  public String getPublicKey() {
    return pubKey;
  }

  public String getPrivateKey() {
    return privKey;
  }
}
