import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";

jest.mock("@react-keycloak/web", () => ({
  useKeycloak: () => ({
    keycloak: { authenticated: false, login: jest.fn() },
    initialized: true,
  }),
}));

test("renders Aurum Core Banking heading", () => {
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>
  );
  expect(screen.getByRole("heading", { level: 1, name: /Aurum Core Banking/i })).toBeInTheDocument();
});
