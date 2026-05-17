Feature: Loan Application and Approval Workflow
  As a retail customer
  I want to apply for a loan
  So that I can finance my home improvement

  Background:
    Given I am authenticated as a banking user

  Scenario: Excellent credit score results in loan approval
    Given a customer with credit score 780 and DTI ratio 0.25
    When they apply for a loan of 20000.00 EUR for 60 months
    Then the credit scoring decision should be "APPROVED"

  Scenario: Poor credit score results in loan decline
    Given a customer with credit score 520 and DTI ratio 0.40
    When they apply for a loan of 10000.00 EUR for 36 months
    Then the credit scoring decision should be "DECLINED"

  Scenario: High DTI ratio overrides good credit score and declines loan
    Given a customer with credit score 700 and DTI ratio 0.55
    When they apply for a loan of 15000.00 EUR for 48 months
    Then the credit scoring decision should be "DECLINED"

  Scenario: PEP customer is sent for manual review regardless of credit score
    Given a customer with credit score 800 and DTI ratio 0.20 who is a PEP
    When they apply for a loan of 50000.00 EUR for 120 months
    Then the credit scoring decision should be "REVIEW"

  Scenario: Fair credit score sends application for manual review
    Given a customer with credit score 620 and DTI ratio 0.35
    When they apply for a loan of 5000.00 EUR for 24 months
    Then the credit scoring decision should be "REVIEW"
