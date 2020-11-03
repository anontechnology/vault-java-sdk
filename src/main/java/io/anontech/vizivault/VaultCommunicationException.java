package io.anontech.vizivault;

/**
 * Indicates that the vault server did not produce a valid response.
 */
public class VaultCommunicationException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public VaultCommunicationException(Throwable cause) {
    super(cause);
  }
}
