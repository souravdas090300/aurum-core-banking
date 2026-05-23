import Keycloak from 'keycloak-js';

// Keycloak configuration
const keycloakConfig = {
  url: 'http://localhost:8180/',
  realm: 'banking',
  clientId: 'banking-app',
};

// Initialize Keycloak instance
const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
