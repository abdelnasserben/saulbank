package com.dabel.controller;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.cheque.ChequeFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.exchange.ExchangeFacadeService;
import com.dabel.service.loan.LoanFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Controller
public class DashboardController implements PageTitleConfig {

    private final TransactionFacadeService transactionFacadeService;
    private final CustomerFacadeService customerFacadeService;
    private final ExchangeFacadeService exchangeFacadeService;
    private final LoanFacadeService loanFacadeService;
    private final CardFacadeService cardFacadeService;
    private final ChequeFacadeService chequeFacadeService;

    public DashboardController(TransactionFacadeService transactionFacadeService, CustomerFacadeService customerFacadeService, ExchangeFacadeService exchangeFacadeService, LoanFacadeService loanFacadeService, CardFacadeService cardFacadeService, ChequeFacadeService chequeFacadeService) {
        this.transactionFacadeService = transactionFacadeService;
        this.customerFacadeService = customerFacadeService;
        this.exchangeFacadeService = exchangeFacadeService;
        this.loanFacadeService = loanFacadeService;
        this.cardFacadeService = cardFacadeService;
        this.chequeFacadeService = chequeFacadeService;
    }

    @GetMapping
    public String dashboard(Model model) {

        List<TransactionDto> transactions = transactionFacadeService.getAll();
        List<CustomerDto> customers = customerFacadeService.getAll();
        List<ExchangeDto> exchanges = exchangeFacadeService.getAll();
        List<ChequeRequestDto> chequeRequests = chequeFacadeService.findAllRequests();
        List<CardRequestDto> cardRequests = cardFacadeService.getAllCardRequests();
        List<LoanRequestDto> loanRequests = loanFacadeService.getAllLoanRequests();


        //TODO: transactions stat (only pending or approved deposit, withdraw, transfer)
        TransactionStatistics.Result tranStatsResult = TransactionStatistics.compute(transactions);
        model.addAttribute("depositTodayKmf", tranStatsResult.depositTodayKmf);
        model.addAttribute("withdrawTodayKmf", tranStatsResult.withdrawTodayKmf);
        model.addAttribute("transferTodayKmf", tranStatsResult.transferTodayKmf);
        model.addAttribute("todayTransactionTotalKmf", tranStatsResult.todayTotalKmf);
        model.addAttribute("transactionPercentLabel", tranStatsResult.percentLabel);

        //TODO: exchanges stat (monthly per currency for current year)

        int currentYear = LocalDate.now().getYear();

        List<String> currencies = Stream.of(Currency.values())
                .map(Enum::name)
                .toList();

        // Map to cumulate by devise and month (index 0 -> Jan)
        Map<String, double[]> boughtMap = new HashMap<>();
        Map<String, double[]> soldMap   = new HashMap<>();
        for (String cur : currencies) {
            boughtMap.put(cur, new double[12]);
            soldMap.put(cur, new double[12]);
        }

        for (ExchangeDto ex : exchanges) {
            if(!ex.getStatus().equalsIgnoreCase(Status.APPROVED.code())) continue;

            LocalDateTime created = ex.getCreatedAt();
            if (created == null || created.getYear() != currentYear) continue;

            int monthIndex = created.getMonthValue() - 1;

            String pCur = ex.getPurchaseCurrency();
            if (boughtMap.containsKey(pCur)) {
                boughtMap.get(pCur)[monthIndex] += ex.getPurchaseAmount();
            }

            String sCur = ex.getSaleCurrency();
            if (soldMap.containsKey(sCur)) {
                soldMap.get(sCur)[monthIndex] += ex.getSaleAmount();
            }
        }

        // Convert the arrays to List<Double> and add them at the model with currency attribute.
        // Example of created attribute : boughtMonthExchangesKMF, soldMonthExchangesKMF, boughtMonthExchangesEUR, ...
        for (String cur : currencies) {
            double[] bArr = boughtMap.get(cur);
            double[] sArr = soldMap.get(cur);

            List<Double> bList = new ArrayList<>(12);
            List<Double> sList = new ArrayList<>(12);
            for (int i = 0; i < 12; i++) {
                bList.add(bArr[i]);
                sList.add(sArr[i]);
            }

            model.addAttribute("boughtMonthExchanges" + cur, bList);
            model.addAttribute("soldMonthExchanges"   + cur, sList);
        }
        model.addAttribute("exchangeCurrencies", currencies);


        //TODO: Customers (total & preview)
        int totalCustomers = customers.size();
        List<CustomerDto> customersPreview = customers.stream()
                .limit(6)
                .toList();

        model.addAttribute("customersPreview", customersPreview);
        model.addAttribute("totalCustomers", totalCustomers);


        //TODO: Earned fees stats (daily)
        EnumMap<EarningFeeStatistics.FeeType, Double> todayEarnedFees = EarningFeeStatistics.computeToday(transactions);
        double totalTodayEarnedFeesKmf = todayEarnedFees.values().stream().mapToDouble(Double::doubleValue).sum();

        model.addAttribute("commissionWithdrawToday", todayEarnedFees.get(EarningFeeStatistics.FeeType.WITHDRAWALS));
        model.addAttribute("commissionTransferToday", todayEarnedFees.get(EarningFeeStatistics.FeeType.TRANSFERS));
        model.addAttribute("commissionLoanToday", todayEarnedFees.get(EarningFeeStatistics.FeeType.LOANS));
        model.addAttribute("commissionCardToday", todayEarnedFees.get(EarningFeeStatistics.FeeType.CARDS));
        model.addAttribute("commissionChequeToday", todayEarnedFees.get(EarningFeeStatistics.FeeType.CHEQUES));
        model.addAttribute("totalCommissionToday", totalTodayEarnedFeesKmf);

        //TODO: Pending operations (transactions, exchanges, cards, loans and cheques requests)
        // estimate total size for pré-allocation memory (simple optimization)
        int sizeEstimate = transactions.size() + exchanges.size() + cardRequests.size() + chequeRequests.size() + loanRequests.size();
        List<OperationPreviewDto> pendingOperations = new ArrayList<>(Math.max(10, Math.min(sizeEstimate, 10_000)));

        // transactions
        PendingOperationsHelper.collectPending(
                pendingOperations,
                transactions,
                TransactionDto::getTransactionId,
                "Transaction",
                TransactionDto::getStatus,
                TransactionDto::getInitiatedBy,
                TransactionDto::getCreatedAt,
                "/transactions"
        );

        // exchanges
        PendingOperationsHelper.collectPending(
                pendingOperations,
                exchanges,
                ExchangeDto::getExchangeId,
                "Exchange",
                ExchangeDto::getStatus,
                ExchangeDto::getInitiatedBy,
                ExchangeDto::getCreatedAt,
                "/exchanges"
        );

        // card requests
        PendingOperationsHelper.collectPending(
                pendingOperations,
                cardRequests,
                CardRequestDto::getRequestId,
                "Card Request",
                CardRequestDto::getStatus,
                CardRequestDto::getInitiatedBy,
                CardRequestDto::getCreatedAt,
                "/cards/requests"
        );

        // cheque requests
        PendingOperationsHelper.collectPending(
                pendingOperations,
                chequeRequests,
                ChequeRequestDto::getRequestId,
                "Cheque Request",
                ChequeRequestDto::getStatus,
                ChequeRequestDto::getInitiatedBy,
                ChequeRequestDto::getCreatedAt,
                "/cheques/requests"
        );

        // loan requests
        PendingOperationsHelper.collectPending(
                pendingOperations,
                loanRequests,
                LoanRequestDto::getRequestId,
                "Loan Request",
                LoanRequestDto::getStatus,
                LoanRequestDto::getInitiatedBy,
                LoanRequestDto::getCreatedAt,
                "/loans/requests"
        );

        // filter by createdAt desc (nulls last)
        pendingOperations.sort(
                Comparator.comparing(OperationPreviewDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
        );
        model.addAttribute("pendingOperations", pendingOperations);


        configPageTitle(model, Web.Menu.General.DASHBOARD);
        return Web.View.DASHBOARD;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.General.MENU, null};
    }

    private static final class TransactionStatistics {
        public static final EnumSet<Status> ALLOWED_STATUS = EnumSet.of(Status.APPROVED);
        public static final EnumSet<TransactionType> ALLOWED_TYPES = EnumSet.of(
                TransactionType.DEPOSIT, TransactionType.WITHDRAW, TransactionType.TRANSFER);
        public static final String KMF = Currency.KMF.name();

        public static class Result {
            public double depositTodayKmf = 0d;
            public double withdrawTodayKmf = 0d;
            public double transferTodayKmf = 0d;
            public double todayTotalKmf = 0d;
            public double yesterdayTotalKmf = 0d;
            public double percentChange = 0d;
            public String percentLabel = "0,0%";
        }

        public static Result compute(List<TransactionDto> transactions) {
            Result r = new Result();
            if (transactions == null || transactions.isEmpty()) return r;

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            for (TransactionDto t : transactions) {
                Status s = Status.valueOf(Status.nameOf(t.getStatus()));
                TransactionType tt = TransactionType.valueOf(t.getTransactionType());

                if (!ALLOWED_STATUS.contains(s) || !ALLOWED_TYPES.contains(tt)) continue;

                LocalDate txDate = t.getCreatedAt().toLocalDate();

                double amountKmf = CurrencyExchanger.exchange(t.getCurrency(), KMF, t.getAmount());

                if (txDate.equals(today)) {
                    switch (tt) {
                        case DEPOSIT -> r.depositTodayKmf += amountKmf;
                        case WITHDRAW -> r.withdrawTodayKmf += amountKmf;
                        case TRANSFER -> r.transferTodayKmf += amountKmf;
                    }
                } else if (txDate.equals(yesterday)) {
                    r.yesterdayTotalKmf += amountKmf;
                }
            }

            r.todayTotalKmf = r.depositTodayKmf + r.withdrawTodayKmf + r.transferTodayKmf;

            r.percentChange = ((r.todayTotalKmf - r.yesterdayTotalKmf) / r.yesterdayTotalKmf) * 100d;
            r.percentLabel = formatPercent(r.percentChange);

            return r;
        }

        private static String formatPercent(double percent) {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.FRANCE);
            DecimalFormat df = new DecimalFormat("+#0.0;-#0.0", symbols);
            return df.format(percent) + "%";
        }
    }

    private static class EarningFeeStatistics {

        public enum FeeType {
            WITHDRAWALS,
            TRANSFERS,
            LOANS,
            CARDS,
            CHEQUES,
            UNKNOWN
        }


        public static EnumMap<FeeType, Double> computeToday(List<TransactionDto> transactions) {
            EnumMap<FeeType, Double> map = new EnumMap<>(FeeType.class);
            for (FeeType t : FeeType.values()) map.put(t, 0d);

            if (transactions == null || transactions.isEmpty()) return map;

            LocalDate today = LocalDate.now();

            for (TransactionDto t : transactions) {
                if (!t.getTransactionType().equalsIgnoreCase(TransactionType.FEE.name())) continue;

                LocalDate txDate = t.getCreatedAt().toLocalDate();

                if (!txDate.equals(today)) continue; // only today

                FeeType cType = detectCommissionType(t.getReason());
                map.put(cType, t.getAmount());
            }

            return map;
        }

        private static FeeType detectCommissionType(String reason) {
            if (reason == null || reason.trim().isEmpty()) return FeeType.UNKNOWN;
            String r = reason.toLowerCase(Locale.ROOT);

            if (r.contains("withdraw")) return FeeType.WITHDRAWALS;
            if (r.contains("transfer")) return FeeType.TRANSFERS;
            if (r.contains("loan")) return FeeType.LOANS;
            if (r.contains("card")) return FeeType.CARDS;
            if (r.contains("cheque")) return FeeType.CHEQUES;

            return FeeType.UNKNOWN;
        }

    }

    public static class PendingOperationsHelper {

        private static final String PENDING = "PENDING";
        private static final String MISSING_OPERATOR = "—";

        public static <T, ID> void collectPending(
                List<OperationPreviewDto> out,
                Collection<T> items,
                Function<T, ID> idFn,
                String typeLabel,
                Function<T, String> statusFn,
                Function<T, String> initiatedByFn,
                Function<T, LocalDateTime> createdAtFn,
                String path
        ) {
            if (items == null || items.isEmpty()) return;

            for (T item : items) {
                if (item == null) continue;
                String status = Status.nameOf(statusFn.apply(item));
                if (!PENDING.equalsIgnoreCase(status)) continue;

                ID id = idFn.apply(item);
                String operator = ofNullable(initiatedByFn.apply(item)).orElse(MISSING_OPERATOR);
                LocalDateTime date = createdAtFn.apply(item);
                String url = path + "/" + id;

                out.add(new OperationPreviewDto((Long) id, typeLabel, operator, date, status, url));
            }
        }
    }

    public record OperationPreviewDto(Long operationId, String operationType, String operator, LocalDateTime createdAt, String status, String url) {
    }

}
