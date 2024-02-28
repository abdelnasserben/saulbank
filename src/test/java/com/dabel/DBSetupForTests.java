package com.dabel;

import com.dabel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBSetupForTests {
    @Autowired
    CardRequestRepository cardRequestRepository;
    @Autowired
    CardRepository cardRepository;
    @Autowired
    LoanRepository loanRepository;
    @Autowired
    ExchangeRepository exchangeRepository;
    @Autowired
    LedgerRepository ledgerRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    TrunkRepository trunkRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BranchRepository branchRepository;

    public void truncate() {
        cardRequestRepository.deleteAll();
        cardRepository.deleteAll();
        loanRepository.deleteAll();
        exchangeRepository.deleteAll();
        ledgerRepository.deleteAll();
        transactionRepository.deleteAll();
        trunkRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
        branchRepository.deleteAll();
    }
}
