@E2E
Feature: budget management
	Scenario: client creates budgets for a user
		Given a user named Alice with email alice@rebels.org
		When the client creates a budget for month "2026-03" with amount 1500.0 RON for alice@rebels.org
		And the client creates a budget for month "2026-04" with amount 2000.0 RON for alice@rebels.org
		Then the client can retrieve 2 budget(s) for alice@rebels.org

	Scenario: client retrieves all budgets
		Given a user named Anakin with email anakin@jedi.org
		And a user named Obi-Wan with email obiwan@jedi.org
		And the client creates a budget for month "2026-01" with amount 1800.0 RON for anakin@jedi.org
		And the client creates a budget for month "2026-01" with amount 2100.0 RON for obiwan@jedi.org
		When the client retrieves all budgets
		Then the client can see at least 2 budgets

	Scenario: client retrieves budget by id
		Given a user named Yoda with email yoda@jedi.org
		And a budget for month "2026-07" with amount 3000.0 RON for yoda@jedi.org exists
		When the client retrieves the budget by id
		Then the budget response status code is 200
		And the budget has month "2026-07" with amount 3000.0 and currency RON

	Scenario: client updates an existing budget
		Given a user named Mace with email mace@jedi.org
		And a budget for month "2026-08" with amount 1600.0 RON for mace@jedi.org exists
		When the client updates the budget amount to 1900.0
		Then the budget response status code is 200
		And the budget has month "2026-08" with amount 1900.0 and currency RON

	Scenario: client deletes a budget
		Given a user named Padme with email padme@naboo.org
		And a budget for month "2026-09" with amount 1400.0 RON for padme@naboo.org exists
		When the client deletes the budget
		Then the budget response status code is 204
		When the client tries to retrieve the deleted budget by id
		Then the budget response status code is 404

	Scenario: client views budget aggregates after update
		Given a user named Bob with email bob@rebels.org
		And the client creates a budget for month "2026-05" with amount 900.0 RON for bob@rebels.org
		And the client creates a budget for month "2026-06" with amount 1100.0 RON for bob@rebels.org
		When the client updates the budget for month "2026-05" to amount 1200.0 RON for bob@rebels.org
		Then the client can retrieve total budget amount 2300.0 for bob@rebels.org
		And the client can retrieve average budget amount 1150.0 for bob@rebels.org
		And the highest budget amount for bob@rebels.org is 1200.0

	Scenario: client cannot retrieve a non-existent budget
		When the client retrieves budget id nonexistent-budget-id
		Then the budget response status code is 404

	Scenario: client cannot delete a non-existent budget
		When the client deletes budget id nonexistent-budget-id
		Then the budget response status code is 404
