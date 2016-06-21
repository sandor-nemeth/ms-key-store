Feature: store and retrieve keys

  Scenario: store and retrieve a key
    Given a key at-testkey and a value at-testvalue
    When I store the key
    Then I can retrieve the value via the HTTP API