package com.dabel;

import com.dabel.constant.*;
import com.dabel.constant.Currency;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.exchange.ExchangeFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
import com.dabel.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private static final String DEFAULT_AVATARS_PATH = "src/main/resources/static/default_avatars/";
    private static final String DEFAULT_SIGNATURES_PATH = "src/main/resources/static/default_signatures/";
    private static final String ASSET_AVATARS_PATH = "src/main/resources/static/assets/avatars/";
    private static final String ASSET_SIGNATURES_PATH = "src/main/resources/static/assets/signatures/";

    private static final String[] CUSTOMERS_IDENTITY_NUMBERS = {
            "NIN001234","NBE100237","NIN450912","NBE209876","NIN378456",
            "NBE567324","NIN876453","NBE023945","NIN034512","NBE256748",
            "NIN563291","NBE432876","NIN987654","NBE654321","NIN432109",
            "FRA987654","USA123456","SEN456789","CIV789012","MAR654321"
    };

    private static final int COUNT_GENERATE_EXCHANGES = 20;
    private static final int COUNT_GENERATE_TRANSACTIONS = 50;
    private static final int BATCH_CHUNK_SIZE = 500;
    private static final String TRANSACTION_TABLE = "transactions";
    private static final String TRANSACTION_ID_COL = "transaction_id";
    private static final String EXCHANGE_TABLE = "exchanges";
    private static final String EXCHANGE_ID_COL = "exchange_id";

    private final BranchFacadeService branchFacadeService;
    private final UserService userService;
    private final CustomerFacadeService customerFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final TransactionFacadeService transactionFacadeService;
    private final ExchangeFacadeService exchangeFacadeService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // 1. create branches
            List<BranchDto> branches = createBranches();

            if (branches.size() < 2) {
                log.warn("Two branches expected. Seed canceled.");
                return;
            }

            BranchDto savedBranch1 = branches.get(0);
            BranchDto savedBranch2 = branches.get(1);

            // 2. create users
            createUsers(savedBranch1, savedBranch2);

            // 3. prepare customers (single list reused)
            List<CustomerDto> customers = customersList();

            // 4. create customers + accounts
            createCustomersWithAccounts(customers, savedBranch1, savedBranch2);

            // 5. copy default avatars & signatures
            copyDefaultCustomerPhotos();

            // 6. generate transactions (deposits, withdrawals, transfers)
            generateAndProcessTransactions(savedBranch1, savedBranch2);

            // 7. generate exchanges
            generateAndProcessExchanges(savedBranch1, savedBranch2, customers);

            // 8. randomize created_at for all transactions & exchanges
            List<Long> txIds = extractIdsFromTables(transactionFacadeService.getAll(), TransactionDto::getTransactionId);
            randomizeCreatedAtForEntities(txIds, TRANSACTION_TABLE, TRANSACTION_ID_COL, 3);

            List<Long> exIds = extractIdsFromTables(exchangeFacadeService.getAll(), ExchangeDto::getExchangeId);
            randomizeCreatedAtForEntities(exIds, EXCHANGE_TABLE, EXCHANGE_ID_COL, 180);

            log.info("DataLoader finished successfully.");
        } catch (Exception ex) {
            log.error("DataLoader failed: {}", ex.getMessage(), ex);
        }
    }

    // ----------------------------
    // Creation helper methods
    // ----------------------------

    private List<BranchDto> createBranches() {
        branchFacadeService.create(BranchDto.builder()
                        .branchName("HQ")
                        .branchAddress("Moroni, Place de la France")
                        .build(),
                new double[]{50_000_000d, 300_000d, 100_000d});

        branchFacadeService.create(BranchDto.builder()
                        .branchName("MUTS")
                        .branchAddress("Mutsamudu, Chitsangani")
                        .build(),
                new double[]{25_000_000d, 180_000d, 100_000d});

        // One call to getAll() and reuse it
        return branchFacadeService.getAll();
    }

    private void createUsers(BranchDto b1, BranchDto b2) {
        UserDto user1 = UserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe@saulbank.com")
                .role("ADMIN")
                .branch(b1)
                .build();

        UserDto user2 = UserDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .username("sarah.hunt@saulbank.com")
                .role("CASHIER")
                .branch(b2)
                .build();

        userService.create(user1);
        userService.create(user2);
    }

    private void createCustomersWithAccounts(List<CustomerDto> customers, BranchDto b1, BranchDto b2) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (CustomerDto customer : customers) {
            String accountName = customer.getFirstName() + " " + customer.getLastName();
            AccountType accountType = rnd.nextBoolean() ? AccountType.SAVING : AccountType.BUSINESS;
            // random branch assignment
            customer.setBranch(rnd.nextBoolean() ? b1 : b2);
            try {
                customerFacadeService.createNewCustomerWithAccount(customer, accountName, accountType, AccountProfile.PERSONAL);
            } catch (Exception ex) {
                log.warn("Can't create customer {} : {}", customer.getIdentityNumber(), ex.getMessage());
            }
        }
    }

    private void copyDefaultCustomerPhotos() {
        CustomersPhotoLoader avatarsLoader = new CustomersPhotoLoader(DEFAULT_AVATARS_PATH, ASSET_AVATARS_PATH);
        avatarsLoader.load();
        CustomersPhotoLoader signaturesLoader = new CustomersPhotoLoader(DEFAULT_SIGNATURES_PATH, ASSET_SIGNATURES_PATH);
        signaturesLoader.load();
    }

    private void generateAndProcessTransactions(BranchDto b1, BranchDto b2) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<TransactionDto> deposits = generateTransactions(b1, b2, TransactionType.DEPOSIT, rnd);
        deposits.forEach(tx -> safeInitTransaction(tx, "DEPOSIT"));

        // approve ~95% of existing transactions (at this moment only deposits are in DB)
        List<TransactionDto> txAfterDeposits = transactionFacadeService.getAll();
        int toApprove = (int) (txAfterDeposits.size() * 0.95);
        txAfterDeposits.stream()
                .limit(toApprove)
                .map(TransactionDto::getTransactionId)
                .forEach(id -> safeApproveTransaction(id, "DEPOSIT approval"));

        // withdrawals
        List<TransactionDto> withdrawals = generateTransactions(b1, b2, TransactionType.WITHDRAW, rnd);
        withdrawals.forEach(tx -> safeInitTransaction(tx, "WITHDRAW"));

        // transfers
        List<TransactionDto> transfers = generateTransactions(b1, b2, TransactionType.TRANSFER, rnd);
        transfers.forEach(tx -> safeInitTransaction(tx, "TRANSFER"));

        // Get all transactions once and approve withdraws & transfers
        List<TransactionDto> allTx = transactionFacadeService.getAll();

        allTx.stream()
                .filter(tx -> TransactionType.WITHDRAW.name().equals(tx.getTransactionType()) ||
                        TransactionType.TRANSFER.name().equals(tx.getTransactionType()))
                .map(TransactionDto::getTransactionId)
                .forEach(id -> safeApproveTransaction(id, "WITHDRAW/TRANSFER approval"));
    }

    private void generateAndProcessExchanges(BranchDto b1, BranchDto b2, List<CustomerDto> customers) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<ExchangeDto> exchanges = generateExchanges(b1, b2, customers, rnd);
        exchanges.forEach(ex -> {
            try {
                exchangeFacadeService.init(ex);
            } catch (Exception e) {
                log.warn("Init exchange failed: {}", e.getMessage());
            }
        });

        // approve all exchanges once
        exchangeFacadeService.getAll().stream()
                .map(ExchangeDto::getExchangeId)
                .forEach(this::safeApproveExchange);
    }

    // ----------------------------
    // Transaction/exchange helpers
    // ----------------------------

    private void safeInitTransaction(TransactionDto tx, String context) {
        try {
            transactionFacadeService.init(tx);
        } catch (Exception ex) {
            log.warn("Error init {} seed: {}", context, ex.getMessage());
        }
    }

    private void safeApproveTransaction(Long transactionId, String context) {
        try {
            transactionFacadeService.approve(transactionId);
        } catch (Exception ex) {
            log.warn("Error approve {} (id={}): {}", context, transactionId, ex.getMessage());
        }
    }

    private void safeApproveExchange(Long exchangeId) {
        try {
            exchangeFacadeService.approve(exchangeId);
        } catch (Exception e) {
            log.warn("Error approving exchange id {} : {}", exchangeId, e.getMessage());
        }
    }

    // ----------------------------
    // Generators
    // ----------------------------

    private List<ExchangeDto> generateExchanges(BranchDto b1, BranchDto b2, List<CustomerDto> customers, ThreadLocalRandom rnd) {
        List<ExchangeDto> generated = new ArrayList<>();
        List<CustomerDto> safeCustomers = Optional.ofNullable(customers).orElseGet(Collections::emptyList);
        Currency[] currencies = Currency.values();

        for (int i = 0; i < COUNT_GENERATE_EXCHANGES; i++) {
            BranchDto operationBranch = rnd.nextBoolean() ? b1 : b2;
            CustomerDto customer = safeCustomers.get(rnd.nextInt(safeCustomers.size()));

            Currency pCurr = currencies[rnd.nextInt(currencies.length)];
            Currency sCurr = currencies[rnd.nextInt(currencies.length)];

            // ensure pCurr != sCurr and one side is KMF
            int attempt = 0;
            while ((pCurr == sCurr || (!pCurr.equals(Currency.KMF) && !sCurr.equals(Currency.KMF))) && attempt++ < 10) {
                sCurr = currencies[rnd.nextInt(currencies.length)];
            }

            double amount = 10 + (rnd.nextDouble() * 1000);

            generated.add(ExchangeDto.builder()
                    .purchaseCurrency(pCurr.name())
                    .purchaseAmount(amount)
                    .saleCurrency(sCurr.name())
                    .customerIdentityNumber(customer.getIdentityNumber())
                    .customerFullName(customer.getFirstName() + " " + customer.getLastName())
                    .branch(operationBranch)
                    .build());
        }
        return generated;
    }

    private List<TransactionDto> generateTransactions(BranchDto savedBranch1,
                                                      BranchDto savedBranch2,
                                                      TransactionType transactionType,
                                                      ThreadLocalRandom rnd) {
        List<TransactionDto> generated = new ArrayList<>();

        List<TrunkDto> allTrunks = accountFacadeService.getAllTrunks();
        if (allTrunks.isEmpty()) {
            log.warn("No trunk found. Can't generate transactions.");
            return generated;
        }

        String[] depositCurrencies = new String[]{"KMF", "EUR", "USD"};

        for (int i = 0; i < COUNT_GENERATE_TRANSACTIONS; i++) {
            TrunkDto trunkA = allTrunks.get(rnd.nextInt(allTrunks.size()));
            AccountDto trunkAAccount = trunkA.getAccount();
            CustomerDto trunkAOwner = trunkA.getCustomer();

            BranchDto operationBranch = rnd.nextBoolean() ? savedBranch1 : savedBranch2;
            double amount = 100 + (rnd.nextDouble() * 10_000);

            String ownerIdentity = trunkAOwner.getIdentityNumber();
            String ownerFullName = trunkAOwner.getFirstName() + " " + trunkAOwner.getLastName();

            TransactionDto tx;
            switch (transactionType) {
                case DEPOSIT -> {
                    String currency = depositCurrencies[rnd.nextInt(depositCurrencies.length)];
                    tx = getTransactionDto(TransactionType.DEPOSIT.name(), currency, amount, trunkAAccount, null,
                            ownerFullName, ownerIdentity, "Seed deposit", operationBranch);
                }

                case WITHDRAW -> tx = getTransactionDto(TransactionType.WITHDRAW.name(), Currency.KMF.name(), amount * 5, trunkAAccount, null,
                        ownerFullName, ownerIdentity, "Seed withdrawal", operationBranch);

                case TRANSFER -> {
                    TrunkDto trunkB = allTrunks.get(rnd.nextInt(allTrunks.size()));
                    while (trunkB.getTrunkId().equals(trunkA.getTrunkId())) {
                        trunkB = allTrunks.get(rnd.nextInt(allTrunks.size()));
                    }
                    AccountDto trunkBAccount = trunkB.getAccount();
                    tx = getTransactionDto(TransactionType.TRANSFER.name(), Currency.KMF.name(), amount * 6, trunkAAccount, trunkBAccount,
                            ownerFullName, ownerIdentity, "Seed transfer", operationBranch);
                }
                default -> throw new IllegalStateException("Unexpected value: " + transactionType);
            }
            generated.add(tx);
        }
        return generated;
    }

    private static TransactionDto getTransactionDto(String type, String currency, double amount,
                                                    AccountDto initiatorAccount, AccountDto receiverAccount,
                                                    String ownerFullName, String ownerIdentity, String reason,
                                                    BranchDto operationBranch) {
        return TransactionDto.builder()
                .transactionType(type)
                .currency(currency)
                .amount(amount)
                .initiatorAccount(initiatorAccount)
                .receiverAccount(receiverAccount)
                .customerFullName(ownerFullName)
                .customerIdentity(ownerIdentity)
                .reason(reason)
                .branch(operationBranch)
                .sourceType(SourceType.ONLINE.name())
                .sourceValue(operationBranch.getBranchName())
                .build();
    }

    // ----------------------------
    // Randomize created_at (batch)
    // ----------------------------
    private LocalDateTime randomDateTimeBetweenNowAndDaysBack(int daysBack) {
        Instant now = Instant.now();
        Instant min = now.minus(Duration.ofDays(daysBack));
        long minMillis = min.toEpochMilli();
        long maxMillis = now.toEpochMilli();
        long randomMillis = ThreadLocalRandom.current().nextLong(minMillis, maxMillis + 1);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(randomMillis), ZoneId.systemDefault());
    }

    private <T> List<Long> extractIdsFromTables(List<T> items, Function<T, Long> idExtractor) {
        return items.stream()
                .map(idExtractor)
                .toList();
    }

    private void randomizeCreatedAtForEntities(List<Long> ids, String table, String idCol, int daysBack) {
        if (ids == null || ids.isEmpty()) {
            log.info("No ids to update for table {}.", table);
            return;
        }

        String sql = "UPDATE " + table + " SET created_at = ? WHERE " + idCol + " = ?";

        for (int start = 0; start < ids.size(); start += BATCH_CHUNK_SIZE) {
            int end = Math.min(start + BATCH_CHUNK_SIZE, ids.size());
            List<Long> chunk = ids.subList(start, end);

            List<Timestamp> timestamps = chunk.stream()
                    .map(id -> Timestamp.valueOf(randomDateTimeBetweenNowAndDaysBack(daysBack)))
                    .toList();

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    ps.setTimestamp(1, timestamps.get(i));
                    ps.setLong(2, chunk.get(i));
                }

                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });

            log.info("Updated {} rows in table {} (chunk {}..{}).", chunk.size(), table, start, end - 1);
        }
    }

    // ----------------------------
    // Customers data (full 20 entries)
    // ----------------------------
    public CustomerDto createCustomer(String firstName, String lastName, String gender, String nationality,
                                      String identityType, String identityNumber, LocalDate identityExpiration,
                                      String identityIssue, LocalDate birthDate, String birthPlace,
                                      String profession, String email, String phone, String address, String postCode) {

        String profileAndSignatureImageName = identityNumber + ".jpg";

        return CustomerDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .gender(gender)
                .nationality(nationality)
                .identityType(identityType)
                .identityNumber(identityNumber)
                .identityExpiration(identityExpiration)
                .identityIssue(identityIssue)
                .birthDate(birthDate)
                .birthPlace(birthPlace)
                .profession(profession)
                .email(email)
                .phone(phone)
                .residence(Country.KM.getName())
                .address(address)
                .postCode(postCode)
                .profilePicture(profileAndSignatureImageName)
                .signaturePicture(profileAndSignatureImageName)
                .build();
    }

    private List<CustomerDto> customersList() {
        CustomerDto customer1 = createCustomer("Ali", "Madi", "MALE", Country.KM.getName(), "CARD",
                "NIN001234", LocalDate.of(2030, 8, 12), "Moroni",
                LocalDate.of(1990, 5, 21), "Hopital El Maarouf", "Entrepreneur",
                "ali.madi@gmail.com", "+2693214567", "Rue des Palmiers", "97600");

        CustomerDto customer2 = createCustomer("Fatima", "Said", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE100237", LocalDate.of(2029, 1, 15), "Anjouan",
                LocalDate.of(1993, 12, 30), "Hopital de Hombo", "Teacher",
                "fatima.said@yahoo.com", "+2694432768", "Avenue Ahmed Abdallah", "97610");

        CustomerDto customer3 = createCustomer("Youssouf", "Mohamed", "MALE", Country.KM.getName(), "CARD",
                "NIN450912", LocalDate.of(2028, 3, 25), "Mutsamudu",
                LocalDate.of(1988, 7, 14), "Hopital de Domoni", "Mechanic",
                "youssouf.mohamed@outlook.com", "+2696753120", "Quartier Bazimini", "97620");

        CustomerDto customer4 = createCustomer("Amina", "Bacar", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE209876", LocalDate.of(2031, 6, 5), "Fomboni",
                LocalDate.of(1995, 9, 18), "Centre Médical de Fomboni", "Pharmacist",
                "amina.bacar@gmail.com", "+2698743210", "Rue de la Corniche", "97630");

        CustomerDto customer5 = createCustomer("Houssein", "Abdallah", "MALE", Country.KM.getName(), "CARD",
                "NIN378456", LocalDate.of(2027, 11, 20), "Moroni",
                LocalDate.of(1991, 2, 10), "Hopital El Maarouf", "Civil Engineer",
                "houssein.abdallah@km.com", "+2698746532", "Route Nationale 1", "97640");

        CustomerDto customer6 = createCustomer(
                "Saïda", "Ahmed", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE567324", LocalDate.of(2028, 9, 14), "Anjouan",
                LocalDate.of(1987, 4, 11), "Centre de Santé de Sima", "Nurse",
                "saida.ahmed@gmail.com", "+2693458769", "Rue des Tisserands", "97650"
        );

        CustomerDto customer7 = createCustomer(
                "Ibrahim", "Said", "MALE", Country.KM.getName(), "CARD",
                "NIN876453", LocalDate.of(2032, 2, 22), "Moroni",
                LocalDate.of(1996, 3, 17), "Clinique Salama", "Software Developer",
                "ibrahim.said@protonmail.com", "+2697342185", "Rue des Lotus", "97600"
        );

        CustomerDto customer8 = createCustomer(
                "Fahida", "Ali", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE023945", LocalDate.of(2031, 12, 12), "Fomboni",
                LocalDate.of(1992, 6, 28), "Centre Médical de Fomboni", "Bank Teller",
                "fahida.ali@outlook.com", "+2697564132", "Quartier Djoumoichongo", "97630"
        );

        CustomerDto customer9 = createCustomer(
                "Salim", "Bourhane", "MALE", Country.KM.getName(), "CARD",
                "NIN034512", LocalDate.of(2029, 8, 29), "Mutsamudu",
                LocalDate.of(1990, 7, 23), "Centre Hospitalier de Mutsamudu", "Police Officer",
                "salim.bourhane@km.com", "+2698346217", "Quartier Citadelle", "97620"
        );

        CustomerDto customer10 = createCustomer(
                "Oumou", "Aboubacar", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE256748", LocalDate.of(2027, 4, 20), "Moroni",
                LocalDate.of(1985, 11, 6), "Hopital El Maarouf", "Businesswoman",
                "oumou.aboubacar@gmail.com", "+2696123487", "Rue des Cocotiers", "97600"
        );

        CustomerDto customer11 = createCustomer(
                "Mariam", "Soeuf", "FEMALE", Country.KM.getName(), "CARD",
                "NIN563291", LocalDate.of(2032, 10, 11), "Mutsamudu",
                LocalDate.of(1994, 9, 13), "Hopital de Hombo", "Receptionist",
                "mariam.soeuf@hotmail.com", "+2698532146", "Rue de la Liberté", "97620"
        );

        CustomerDto customer12 = createCustomer(
                "Ahmed", "Assoumani", "MALE", Country.KM.getName(), "PASSPORT",
                "NBE432876", LocalDate.of(2031, 5, 9), "Fomboni",
                LocalDate.of(1989, 10, 22), "Centre Médical de Fomboni", "Accountant",
                "ahmed.assoumani@km.com", "+2699468123", "Avenue Ali Soilihi", "97630"
        );

        CustomerDto customer13 = createCustomer(
                "Aziza", "Mohamed", "FEMALE", Country.KM.getName(), "CARD",
                "NIN987654", LocalDate.of(2029, 11, 17), "Moroni",
                LocalDate.of(1997, 1, 10), "Hopital El Maarouf", "Sales Manager",
                "aziza.mohamed@km.com", "+2699563418", "Rue de la Paix", "97600"
        );

        CustomerDto customer14 = createCustomer(
                "Omar", "Ali", "MALE", Country.KM.getName(), "PASSPORT",
                "NBE654321", LocalDate.of(2033, 2, 4), "Anjouan",
                LocalDate.of(1992, 4, 6), "Hopital de Hombo", "Tour Guide",
                "omar.ali@km.com", "+2698653214", "Quartier Mbéni", "97610"
        );

        CustomerDto customer15 = createCustomer(
                "Shamima", "Abdallah", "FEMALE", Country.KM.getName(), "CARD",
                "NIN432109", LocalDate.of(2028, 7, 23), "Mutsamudu",
                LocalDate.of(1991, 12, 2), "Hopital de Domoni", "Secretary",
                "shamima.abdallah@gmail.com", "+2697349865", "Quartier Mirontsi", "97620"
        );

        CustomerDto customer16 = createCustomer(
                "Jean", "Dupont", "MALE", Country.FR.getName(), "PASSPORT",
                "FRA987654", LocalDate.of(2030, 10, 1), "Paris",
                LocalDate.of(1985, 4, 22), "Hôpital Necker", "Financial Analyst",
                "jean.dupont@gmail.com", "+2693217890", "Rue des Bananiers", "97600"
        );

        CustomerDto customer17 = createCustomer(
                "John", "Smith", "MALE", Country.US.getName(), "PASSPORT",
                "USA123456", LocalDate.of(2028, 5, 15), "New York",
                LocalDate.of(1987, 3, 10), "St. Mary’s Hospital", "Marketing Director",
                "john.smith@yahoo.com", "+2694456789", "Avenue des Coquillages", "97600"
        );

        CustomerDto customer18 = createCustomer(
                "Cheikh", "Diop", "MALE", Country.SN.getName(), "PASSPORT",
                "SEN456789", LocalDate.of(2029, 7, 18), "Dakar",
                LocalDate.of(1990, 9, 20), "Hôpital de Fann", "IT Consultant",
                "cheikh.diop@senegal.com", "+2695678901", "Rue des Jasmins", "97620"
        );

        CustomerDto customer19 = createCustomer(
                "Awa", "Kone", "FEMALE", Country.CI.getName(), "PASSPORT",
                "CIV789012", LocalDate.of(2031, 11, 25), "Abidjan",
                LocalDate.of(1994, 6, 12), "CHU de Treichville", "HR Manager",
                "awa.kone@gmail.com", "+2696789012", "Rue des Manguiers", "97610"
        );

        CustomerDto customer20 = createCustomer(
                "Khalid", "El Amrani", "MALE", Country.MA.getName(), "PASSPORT",
                "MAR654321", LocalDate.of(2027, 4, 30), "Casablanca",
                LocalDate.of(1992, 12, 5), "Clinique Chifa", "Architect",
                "khalid.elamrani@ma.com", "+2697890123", "Avenue des Étoiles", "97630"
        );

        return Arrays.asList(
                customer1, customer2, customer3, customer4, customer5,
                customer6, customer7, customer8, customer9, customer10,
                customer11, customer12, customer13, customer14, customer15,
                customer16, customer17, customer18, customer19, customer20
        );
    }

    private static class CustomersPhotoLoader {
        private final Path defaultDir;
        private final Path destinationDir;

        CustomersPhotoLoader(String DEFAULT_PATH, String DESTINATION_PATH) {
            this.defaultDir = Paths.get(DEFAULT_PATH);
            this.destinationDir = Paths.get(DESTINATION_PATH);
        }

        void load() {
            try {
                if (!Files.exists(defaultDir) || !Files.isDirectory(defaultDir)) {
                    log.warn("Default images directory not found: {}", defaultDir);
                    return;
                }
                Files.createDirectories(destinationDir);

                // Count files in destination (cheap)
                long fileCount = Files.list(destinationDir).count();
                // If already populated (more than 1 file) skip
                if (fileCount > 1) {
                    log.info("Destination {} already populated ({} files). Skip copying.", destinationDir, fileCount);
                    return;
                }

                // Copy each default i.jpg to DESTINATION with identity name
                IntStream.rangeClosed(1, CUSTOMERS_IDENTITY_NUMBERS.length)
                        .forEach(i -> {
                            Path src = defaultDir.resolve(i + ".jpg");
                            Path dst = destinationDir.resolve(CUSTOMERS_IDENTITY_NUMBERS[i - 1] + ".jpg");
                            try {
                                if (Files.exists(src)) {
                                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                                } else {
                                    log.debug("Source image does not exist: {}", src);
                                }
                            } catch (IOException e) {
                                log.warn("Cannot copy {} -> {} : {}", src, dst, e.getMessage());
                            }
                        });

            } catch (IOException e) {
                log.error("Error while loading customer photos: {}", e.getMessage(), e);
            }
        }
    }
}
