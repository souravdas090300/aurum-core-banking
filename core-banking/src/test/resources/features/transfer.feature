Feature: Fund Transfers
  As a banking customer
  I want to transfer funds between accounts
  So that I can pay bills and send money to others

  Background:
    Given I am authenticated as a banking user

  Scenario: Successful transfer between two active accounts
    Given account "ACC-001" has a balance of 5000.00 EUR
    And account "ACC-002" has a balance of 1000.00 EUR
    When I transfer 500.00 EUR from "ACC-001" to "ACC-002" with reference "Rent payment"
    Then the transfer should be completed successfully
    And account "ACC-001" balance should be 4500.00 EUR
    And account "ACC-002" balance should be 1500.00 EUR

  Scenario: Transfer fails when sender has insufficient funds
    Given account "ACC-003" has a balance of 100.00 EUR
    And account "ACC-004" has a balance of 500.00 EUR
    When I transfer 999.00 EUR from "ACC-003" to "ACC-004" with reference "Big payment"
    Then the transfer should fail with "Insufficient funds"

  Scenario: Transfer is idempotent for the same idempotency key
    Given account "ACC-005" has a balance of 3000.00 EUR
    And account "ACC-006" has a balance of 0.00 EUR
    When I transfer 300.00 EUR from "ACC-005" to "ACC-006" with idempotency key "idem-key-001"
    And I transfer 300.00 EUR from "ACC-005" to "ACC-006" with idempotency key "idem-key-001"
    Then account "ACC-005" balance should be 2700.00 EUR

  Scenario: Transfer to a frozen account is rejected
    Given account "ACC-007" has a balance of 2000.00 EUR
    And account "ACC-008" is frozen
    When I transfer 200.00 EUR from "ACC-007" to "ACC-008" with reference "Blocked"
    Then the transfer should fail with "not active"
