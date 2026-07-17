import React from "react";
import ReactDOM from "react-dom/client";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import { BrowserRouter } from "react-router-dom";
import { keycloak } from "./auth/keycloak";
import App from "./App";
import "./index.css";

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);
root.render(
  <React.StrictMode>
    <ReactKeycloakProvider authClient={keycloak} initOptions={{ onLoad: "check-sso" }}>
      <BrowserRouter>
      <App />
      </BrowserRouter>
    </ReactKeycloakProvider>
  </React.StrictMode>
);
