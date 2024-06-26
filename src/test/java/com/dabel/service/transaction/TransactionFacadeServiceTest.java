package com.dabel.service.transaction;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionFacadeServiceTest {
//
//    @Autowired
//    TransactionFacadeService transactionFacadeService;
//
//    @Autowired
//    BranchService branchService;
//
//    @Autowired
//    AccountService accountService;
//
//    @Autowired
//    DBSetupForTests dbSetupForTests;
//
//    TransactionDto transactionDto;
//
//    @BeforeEach
//    void setUp() {
//        dbSetupForTests.truncate();
//        BranchDto savedBranch = branchService.save(BranchDto.builder()
//                .branchName("HQ")
//                .branchAddress("London")
//                .status(Status.ACTIVE.code())
//                .build());
//        AccountDto accountDto1 = accountService.save(AccountDto.builder()
//                .accountName("John Doe")
//                .accountNumber("123456789")
//                .currency(Currency.KMF.name())
//                .balance(25000)
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(savedBranch)
//                .build());
//
//        transactionDto = TransactionDto.builder()
//                .initiatorAccount(accountDto1)
//                .currency(Currency.KMF.name())
//                .amount(500)
//                .sourceType(SourceType.ONLINE.name())
//                .reason("Just a test")
//                .branch(savedBranch)
//                .build();
//    }
//
//    private void createVault() {
//        accountService.save(AccountDto.builder()
//                .accountName("Vault Code 1")
//                .accountNumber("987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .isVault(1)
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//    }
//
//    private void createLedger(LedgerType ledgerType) {
//        accountService.save(LedgerDto.builder()
//                .ledgerType(ledgerType.name())
//                .account(AccountDto.builder()
//                        .accountName("Withdraw ledger")
//                        .accountNumber("0213456587")
//                        .accountType(AccountType.BUSINESS.name())
//                        .accountProfile(AccountProfile.PERSONAL.name())
//                        .currency(Currency.KMF.name())
//                        .branch(transactionDto.getInitiatorAccount().getBranch())
//                        .status(Status.ACTIVE.code())
//                        .build())
//                .build());
//    }
//
//    @Test
//    void shouldInitDeposit() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.DEPOSIT.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getTransactionId()).isGreaterThan(0);
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.DEPOSIT.name());
//        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(25000);
//    }
//
//    @Test
//    void shouldApproveDeposit() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.DEPOSIT.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.approve(savedTransaction.getTransactionId());
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.APPROVED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.DEPOSIT.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(-500);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(25500);
//    }
//
//    @Test
//    void shouldRejectDeposit() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.DEPOSIT.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.reject(savedTransaction.getTransactionId(), "Sample remarks");
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.DEPOSIT.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(0);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(25000);
//        assertThat(expected.getFailureReason()).isEqualTo("Sample remarks");
//    }
//
//    @Test
//    void shouldThrowIllegalOperationExceptionWhenTryInitDepositOfAnInactiveAccount() {
//        //given
//        TransactionDto transactionDto1 = TransactionDto.builder()
//                .transactionType(TransactionType.DEPOSIT.name())
//                .initiatorAccount(AccountDto.builder()
//                        .accountName("John Doe")
//                        .accountNumber("123456789")
//                        .currency(Currency.KMF.name())
//                        .accountType(AccountType.SAVING.name())
//                        .accountProfile(AccountProfile.PERSONAL.name())
//                        .status(Status.PENDING.code())
//                        .build())
//                .currency(Currency.KMF.name())
//                .amount(25000)
//                .reason("Just a test")
//                .build();
//
//        //when
//        Exception expected = assertThrows(IllegalOperationException.class, () -> transactionFacadeService.init(transactionDto1));
//
//        //then
//        assertThat(expected.getMessage()).isEqualTo("Account must be active");
//    }
//
//    @Test
//    void shouldInitWithdraw() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.WITHDRAW.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getTransactionId()).isGreaterThan(0);
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.WITHDRAW.name());
//        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(25000);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(0);
//    }
//
//    @Test
//    void shouldApproveWithdraw() {
//        //given
//        createVault();
//        createLedger(LedgerType.WITHDRAW);
//        transactionDto.setTransactionType(TransactionType.WITHDRAW.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.approve(savedTransaction.getTransactionId());
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.APPROVED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.WITHDRAW.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(24300); //withdraw fees are applied
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(500);
//    }
//
//    @Test
//    void shouldRejectWithdraw() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.WITHDRAW.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.reject(savedTransaction.getTransactionId(), "Sample remarks");
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.WITHDRAW.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(25000);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(0);
//        assertThat(expected.getFailureReason()).isEqualTo("Sample remarks");
//    }
//
//    @Test
//    void shouldThrowIllegalOperationExceptionWhenTryInitWithdrawOfAnInactiveAccount() {
//        //given
//        TransactionDto transactionDto1 = TransactionDto.builder()
//                .transactionType(TransactionType.WITHDRAW.name())
//                .initiatorAccount(AccountDto.builder()
//                        .accountName("John Doe")
//                        .accountNumber("123456789")
//                        .currency(Currency.KMF.name())
//                        .accountType(AccountType.SAVING.name())
//                        .accountProfile(AccountProfile.PERSONAL.name())
//                        .status(Status.PENDING.code())
//                        .build())
//                .currency(Currency.KMF.name())
//                .amount(25000)
//                .reason("Just a test")
//                .build();
//
//        //when
//        Exception expected = assertThrows(IllegalOperationException.class, () -> transactionFacadeService.init(transactionDto1));
//
//        //then
//        assertThat(expected.getMessage()).isEqualTo("Account must be active");
//    }
//
//    @Test
//    void shouldThrowBalanceInsufficientExceptionWhenTryInitWithdrawWithInsufficientBalance() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.WITHDRAW.name());
//        transactionDto.setAmount(80000);
//
//        //when
//        Exception expected = assertThrows(BalanceInsufficientException.class, () -> transactionFacadeService.init(transactionDto));
//
//        //then
//        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient");
//    }
//
//    @Test
//    void shouldInitTransfer() {
//        //given
//        AccountDto receiverAccount = accountService.save(AccountDto.builder()
//                .accountName("Sarah Hunt")
//                .accountNumber("0987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionDto.setReceiverAccount(receiverAccount);
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getTransactionId()).isGreaterThan(0);
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.TRANSFER.name());
//        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(25000);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(0);
//    }
//
//    @Test
//    void shouldApproveTransfer() {
//        //given
//        AccountDto receiverAccount = accountService.save(AccountDto.builder()
//                .accountName("Sarah Hunt")
//                .accountNumber("0987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionDto.setReceiverAccount(receiverAccount);
//
//        createLedger(LedgerType.TRANSFER);
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.approve(savedTransaction.getTransactionId());
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.APPROVED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.TRANSFER.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(23975); //transfer fees are applied
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(500);
//    }
//
//    @Test
//    void shouldRejectTransfer() {
//        //given
//        AccountDto receiverAccount = accountService.save(AccountDto.builder()
//                .accountName("Sarah Hunt")
//                .accountNumber("0987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionDto.setReceiverAccount(receiverAccount);
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto savedTransaction = transactionFacadeService.findAll().get(0);
//        transactionFacadeService.reject(savedTransaction.getTransactionId(), "Sample remarks");
//        TransactionDto expected = transactionFacadeService.findAll().get(0);
//
//        //then
//        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.TRANSFER.name());
//        assertThat(expected.getInitiatorAccount().getBalance()).isEqualTo(25000);
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(0);
//        assertThat(expected.getFailureReason()).isEqualTo("Sample remarks");
//    }
//
//    @Test
//    void shouldThrowIllegalOperationExceptionWhenTryInitTransferOfAnInactiveAccount() {
//        //given
//        TransactionDto transactionDto1 = TransactionDto.builder()
//                .transactionType(TransactionType.TRANSFER.name())
//                .initiatorAccount(AccountDto.builder()
//                        .accountName("John Doe")
//                        .accountNumber("123456789")
//                        .currency(Currency.KMF.name())
//                        .accountType(AccountType.SAVING.name())
//                        .accountProfile(AccountProfile.PERSONAL.name())
//                        .status(Status.PENDING.code())
//                        .build())
//                .currency(Currency.KMF.name())
//                .amount(25000)
//                .reason("Just a test")
//                .build();
//
//        //when
//        Exception expected = assertThrows(IllegalOperationException.class, () -> transactionFacadeService.init(transactionDto1));
//
//        //then
//        assertThat(expected.getMessage()).isEqualTo("Initiator account must be active");
//    }
//
//    @Test
//    void shouldThrowBalanceInsufficientExceptionWhenTryInitTransferWithInsufficientBalance() {
//        //given
//        AccountDto receiverAccount = accountService.save(AccountDto.builder()
//                .accountName("Sarah Hunt")
//                .accountNumber("0987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionDto.setReceiverAccount(receiverAccount);
//        transactionDto.setAmount(80000);
//
//        //when
//        Exception expected = assertThrows(BalanceInsufficientException.class, () -> transactionFacadeService.init(transactionDto));
//
//        //then
//        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient");
//    }
//
//    @Test
//    void shouldFindAllByAccount() {
//        //given
//        AccountDto receiverAccount = accountService.save(AccountDto.builder()
//                .accountName("Sarah Hunt")
//                .accountNumber("0987654321")
//                .currency(Currency.KMF.name())
//                .accountType(AccountType.SAVING.name())
//                .accountProfile(AccountProfile.PERSONAL.name())
//                .status(Status.ACTIVE.code())
//                .branch(transactionDto.getInitiatorAccount().getBranch())
//                .build());
//        transactionDto.setTransactionType(TransactionType.TRANSFER.name());
//        transactionDto.setReceiverAccount(receiverAccount);
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        List<TransactionDto> expected = transactionFacadeService.findAllByAccount(receiverAccount);
//
//        //then
//        assertThat(expected.size()).isEqualTo(1);
//        assertThat(expected.get(0).getTransactionType()).isEqualTo(TransactionType.TRANSFER.name());
//        assertThat(expected.get(0).getStatus()).isEqualTo(Status.PENDING.code());
//        assertThat(expected.get(0).getReceiverAccount().getAccountNumber()).isEqualTo("0987654321");
//    }
//
//    @Test
//    void shouldFindById() {
//        //given
//        createVault();
//        transactionDto.setTransactionType(TransactionType.DEPOSIT.name());
//        transactionFacadeService.init(transactionDto);
//
//        //when
//        TransactionDto expected = transactionFacadeService.findById(1L);
//
//        //then
//        assertThat(expected.getTransactionId()).isGreaterThan(0);
//        assertThat(expected.getTransactionType()).isEqualTo(TransactionType.DEPOSIT.name());
//        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
//        assertThat(expected.getReceiverAccount().getBalance()).isEqualTo(25000);
//    }
}