package io.transwarp.flume.sink;

public class KerberosUser {

  private final String principal;
  private final String keyTab;

  public KerberosUser(String principal, String keyTab) {
    this.principal = principal;
    this.keyTab = keyTab;
  }

  public String getPrincipal() {
    return principal;
  }

  public String getKeyTab() {
    return keyTab;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KerberosUser other = (KerberosUser) obj;
    if ((this.principal == null) ? (other.principal != null) : !this.principal.equals(other.principal)) {
      return false;
    }
    if ((this.keyTab == null) ? (other.keyTab != null) : !this.keyTab.equals(other.keyTab)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + (this.principal != null ? this.principal.hashCode() : 0);
    hash = 41 * hash + (this.keyTab != null ? this.keyTab.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "{ principal: " + principal + ", keytab: " + keyTab + " }";
  }
}
