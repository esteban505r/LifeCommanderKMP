# Finance ViewModel Refactoring

## Overview

The monolithic `FinanceViewModel` has been split into separate, focused ViewModels following the Single Responsibility Principle. This improves code maintainability, testability, and separation of concerns.

## New Architecture

### ViewModels Created

1. **AccountViewModel** - Manages account-related state and operations
   - State: `AccountState`
   - Operations: getAccounts, addAccount, updateAccount, deleteAccount, selectAccount

2. **TransactionViewModel** - Manages transaction-related state and operations
   - State: `TransactionState`
   - Operations: getTransactions, addTransaction, updateTransaction, deleteTransaction, changeTransactionFilters, importTransactions, previewTransactionImport

3. **BudgetViewModel** - Manages budget-related state and operations
   - State: `BudgetState`
   - Operations: getBudgets, addBudget, updateBudget, deleteBudget, getBudgetProgress, getBudgetTransactions, changeBudgetFilters, changeBudgetBaseDate, categorizeAll, categorizeUnbudgeted

4. **ScheduledTransactionViewModel** - Manages scheduled transaction-related state and operations
   - State: `ScheduledTransactionState`
   - Operations: getScheduledTransactions, addScheduledTransaction, updateScheduledTransaction, deleteScheduledTransaction, changeTransactionFilters

5. **FinanceCoordinatorViewModel** - Manages tab selection and coordinates between ViewModels
   - State: `FinanceCoordinatorState`
   - Operations: setSelectedTab

### Supporting Classes

- **FinanceActionsWrapper** - Implements `FinanceActions` interface and delegates to appropriate ViewModels
- **FinanceStateConverter** - Combines states from all ViewModels into the unified `DesktopFinanceState` expected by `FinanceScreen`

## Benefits

1. **Single Responsibility** - Each ViewModel has a clear, focused purpose
2. **Better Testability** - Smaller, focused ViewModels are easier to test
3. **Improved Maintainability** - Changes to one domain don't affect others
4. **Clearer Code Organization** - Related functionality is grouped together
5. **Easier to Extend** - Adding new features to one domain doesn't require modifying a large ViewModel

## Migration Notes

### For UI Components

The `FinanceDestination` composable now:
- Injects all ViewModels separately
- Combines their states using `FinanceStateConverter`
- Uses `FinanceActionsWrapper` to provide a unified `FinanceActions` interface

### Backward Compatibility

The old `FinanceViewModel` class still exists but is no longer used. It can be removed in a future cleanup.

## File Structure

```
finance_presentation/
├── ui/
│   ├── viewmodel/
│   │   ├── AccountViewModel.kt
│   │   ├── TransactionViewModel.kt
│   │   ├── BudgetViewModel.kt
│   │   ├── ScheduledTransactionViewModel.kt
│   │   ├── FinanceCoordinatorViewModel.kt
│   │   ├── FinanceActionsWrapper.kt
│   │   └── FinanceViewModel.kt (deprecated)
│   └── viewmodel/state/
│       ├── AccountState.kt
│       ├── TransactionState.kt
│       ├── BudgetState.kt
│       ├── ScheduledTransactionState.kt
│       └── FinanceCoordinatorState.kt
├── converter/
│   └── FinanceStateConverter.kt (updated)
└── navigation/
    └── FinanceDestination.kt (updated)
```

## Future Improvements

1. **SavingsGoalViewModel** - Currently Savings Goals are not implemented in the new architecture (marked as TODO in FinanceActionsWrapper)
2. **Shared State Management** - Consider using a shared state holder for data that needs to be accessed across ViewModels (e.g., selectedAccount affecting transactions)
3. **Dependency Injection** - The FinanceActionsWrapper could be provided via DI instead of being created in the composable

## Testing

Each ViewModel can now be tested independently:
- Unit tests for each ViewModel's business logic
- Integration tests for the FinanceActionsWrapper coordination
- UI tests for the FinanceDestination composable

