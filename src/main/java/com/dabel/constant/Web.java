package com.dabel.constant;

public final class Web {

    public interface Endpoint {
        String BRANCHES = "/branches";
        String BRANCH_ACCOUNTS = BRANCHES + "/accounts";

        String CUSTOMERS = "/customers";
        String CUSTOMER_ADD = CUSTOMERS + "/add";

        String TRANSACTIONS = "/transactions";
        String TRANSACTION_INIT = TRANSACTIONS + "/init";
        String TRANSACTION_APPROVE = TRANSACTIONS + "/approve";
        String TRANSACTION_REJECT = TRANSACTIONS + "/reject";

        String EXCHANGES = "/exchanges";
        String EXCHANGE_INIT = EXCHANGES + "/init";
        String EXCHANGE_APPROVE = EXCHANGES + "/approve";
        String EXCHANGE_REJECT = EXCHANGES + "/reject";

        String LOANS = "/loans";
        String LOAN_INIT = LOANS + "/init";
        String LOAN_APPROVE = LOANS + "/approve";
        String LOAN_REJECT = LOANS + "/reject";

        String CARDS = "/cards";
        String CARD_ACTIVATE = CARDS + "/activate";
        String CARD_DEACTIVATE = CARDS + "/deactivate";
        String CARD_REQUESTS = CARDS + "/requests";
        String CARD_REQUEST_APPROVE = CARD_REQUESTS + "/approve";
        String CARD_REQUEST_REJECT = CARD_REQUESTS + "/reject";

        String ACCOUNTS = "/accounts";
        String ACCOUNT_ACTIVATE = ACCOUNTS + "/activate";
        String ACCOUNT_DEACTIVATE = ACCOUNTS + "/deactivate";
        String ACCOUNT_AFFILIATION = ACCOUNTS + "/affiliation";

        String CHEQUES = "/cheques";
        String CHEQUE_PAY = CHEQUES + "/pay";
        String CHEQUE_REQUESTS = CHEQUES + "/requests";
        String CHEQUE_REQUEST_APPROVE = CHEQUE_REQUESTS + "/approve";
        String CHEQUE_REQUEST_REJECT = CHEQUE_REQUESTS + "/reject";

        String PAGE_404 = "/404";
    }

    public interface View {
        String BRANCHES = "branches";
        String BRANCH_ACCOUNTS = "branches-accounts";
        String DASHBOARD = "dashboard";

        String CUSTOMERS = "customers";
        String CUSTOMER_ADD = "customers-add";
        String CUSTOMER_DETAILS = "customers-details";

        String TRANSACTIONS = "transactions";
        String TRANSACTION_INIT = "transactions-init";
        String TRANSACTION_DETAILS = "transactions-details";

        String EXCHANGES = "exchanges";
        String EXCHANGE_INIT = "exchanges-init";
        String EXCHANGE_DETAILS = "exchanges-details";

        String LOANS = "loans";
        String LOAN_INIT = "loans-init";
        String LOAN_DETAILS = "loans-details";

        String CARDS = "cards";
        String CARD_DETAILS = "cards-details";
        String CARD_REQUESTS = "cards-requests";
        String CARD_REQUEST_DETAILS = "cards-requests-details";

        String ACCOUNTS = "accounts";
        String ACCOUNT_DETAILS = "accounts-details";
        String ACCOUNT_AFFILIATION = "accounts-affiliations";
        String ACCOUNT_AFFILIATION_ADD = "accounts-affiliations-add";

        String CHEQUES = "cheques";
        String CHEQUE_REQUESTS = "cheques-requests";
        String CHEQUES_REQUEST_DETAILS = "cheques-requests-details";
        String CHEQUE_PAY = "cheques-pay";

        String PAGE_404 = "page404";
    }

    public interface MessageTag {
        String ERROR = "errorMessage";
        String SUCCESS = "successMessage";
    }

    public interface Menu {

        interface Bank {
            String MENU = "Bank";

            interface Branches {
                String SUB_MENU = "Branches";
                String ROOT = "Branches List";
                String ACCOUNTS = "Vaults/GL";
            }
        }

        interface General {
            String MENU = "General";
            String DASHBOARD = "Dashboard";
        }

        interface Customer {
            String MENU = "Customers";
            String ROOT = "All Customers";
            String ADD = "New Customer";
        }
        
        interface Transaction {
            String MENU = "Transactions";
            String ROOT = "All Transactions";
            String INIT = "Init Transaction";
        }

        interface Exchange {
            String MENU = "Exchanges";
            String ROOT = "All Exchanges";
            String INIT = "Init Exchange";
        }

        interface Loan {
            String MENU = "Loans";
            String ROOT = "All Loans";
            String INIT = "Init Loan";
        }

        interface Card {
            String MENU = "Cards";
            String ROOT = "All Cards";
            String REQUESTS = "Card Requests";
        }

        interface Account {
            String MENU = "Accounts";
            String ROOT = "All Accounts";
            String AFFILIATION = "Manage Affiliation";
            String AFFILIATION_ADD = "Add Affiliate";
        }

        interface Cheque {
            String MENU = "Cheques";
            String ROOT = "All Cheques";
            String PAY = "Pay A Cheque";
            String REQUESTS = "Cheque Requests";
        }
    }
}