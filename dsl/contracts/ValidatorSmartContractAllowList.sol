// Implementation of a contract to select validators using an allowlist

pragma solidity >=0.5.0;

import "./ValidatorSmartContractInterface.sol";

contract ValidatorSmartContractAllowList is ValidatorSmartContractInterface {

    uint constant MAX_VALIDATORS = 256;
    address[] private validators;

    constructor (address[] memory initialValidators) {
        require(initialValidators.length > 0, "no initial validators");
        require(initialValidators.length < MAX_VALIDATORS, "number of validators cannot be larger than 256");

        for (uint i = 0; i < initialValidators.length; i++) {
            validators.push(initialValidators[i]);
        }
    }

    function getValidators() override external view returns (address[] memory) {
        return validators;
    }

    function activate(address newValidator) external {
        require(newValidator != address(0), "cannot activate validator with address 0");

        for (uint i = 0; i < validators.length; i++) {
            require(newValidator != validators[i], "validator is already active");
        }

        validators.push(newValidator);
    }

    function deactivate(address oldValidator) external {
        require(validators.length > 1, "cannot deactivate last validator");

        for (uint i = 0; i < validators.length; i++) {
            if(oldValidator == validators[i]) {
                validators[i] = validators[validators.length - 1];
                validators.pop();
            }
        }
    }
}