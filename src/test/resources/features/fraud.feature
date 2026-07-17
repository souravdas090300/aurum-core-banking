Feature: Fraud Detection Rules
  As a compliance officer
  I want transactions to be screened by our fraud detection rules
  So that we meet our FIAU and AML regulatory obligations

  Scenario: Large transaction triggers FIAU mandatory report
    Given a transaction of 12000.00 EUR from "MT" to "MT"
    When the fraud rules are evaluated
    Then a FIAU mandatory report flag should be raised
    And the transaction should not be blocked

  Scenario: High velocity of transactions triggers fraud block
    Given a customer has made 6 transactions in the last hour
    And a new transaction of 100.00 EUR is initiated
    When the fraud rules are evaluated
    Then the transaction should be blocked with reason containing "VELOCITY"

  Scenario: Off-hours large transfer raises a fraud flag
    Given a transaction of 6000.00 EUR is initiated at 23:00
    When the fraud rules are evaluated
    Then the transaction should have fraud flag "OFF_HOURS_LARGE_TRANSFER"

  Scenario: Transaction to a sanctioned country is hard-blocked
    Given a transaction of 500.00 EUR from "MT" to "IR"
    When the fraud rules are evaluated
    Then the transaction should be blocked with reason containing "SANCTIONS"
