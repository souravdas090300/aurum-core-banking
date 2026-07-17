import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: process.env.REACT_APP_KEYCLOAK_URL || "http://localhost:8180/auth",
  realm: process.env.REACT_APP_KEYCLOAK_REALM || "banking",
  clientId: process.env.REACT_APP_KEYCLOAK_CLIENT_ID || "banking-api",
});
