package com.dabel.constant;

public final class Web {

    public interface Endpoint {

        String VIEW360 = "/view360";
        String VIEW360_BRANCHES = VIEW360 + "/branches";
        String VIEW360_VAULT_GL = VIEW360_BRANCHES + "/accounts";
        String VIEW360_ACCOUNTS = VIEW360 + "/accounts";
        String VIEW360_TRANSACTIONS = VIEW360 + "/transactions";

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
        String LOAN_REQUEST = LOANS + "/request";
        String LOAN_REQUESTS = LOANS + "/requests";
        String LOAN_REQUESTS_APPROVE = LOAN_REQUESTS + "/approve";
        String LOAN_REQUESTS_REJECT = LOAN_REQUESTS + "/reject";

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
        String CHEQUE_ACTIVATE = CHEQUES + "/activate";
        String CHEQUE_DEACTIVATE = CHEQUES + "/deactivate";
        String CHEQUE_REQUESTS = CHEQUES + "/requests";
        String CHEQUE_REQUEST_APPROVE = CHEQUE_REQUESTS + "/approve";
        String CHEQUE_REQUEST_REJECT = CHEQUE_REQUESTS + "/reject";
        String CHEQUE_PAY = CHEQUES + "/pay";

        String USERS = "/users";


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
        String LOAN_REQUEST = "loans-request";
        String LOAN_DETAILS = "loans-details";
        String LOAN_REQUESTS = "loans-requests";
        String LOAN_REQUEST_DETAILS = "loans-requests-details";

        String CARDS = "cards";
        String CARD_DETAILS = "cards-details";
        String CARD_REQUESTS = "cards-requests";
        String CARD_REQUEST_DETAILS = "cards-requests-details";

        String ACCOUNTS = "accounts";
        String ACCOUNT_DETAILS = "accounts-details";
        String ACCOUNT_AFFILIATION = "accounts-affiliations";
        String ACCOUNT_AFFILIATION_ADD = "accounts-affiliations-add";

        String CHEQUES = "cheques";
        String CHEQUE_DETAILS = "cheques-details";
        String CHEQUE_REQUESTS = "cheques-requests";
        String CHEQUES_REQUEST_DETAILS = "cheques-requests-details";
        String CHEQUE_PAY = "cheques-pay";

        String VIEW360_ACCOUNTS = "view360-accounts";

        String USERS = "users";

        String PAGE_404 = "page404";
    }

    public interface MessageTag {
        String ERROR = "errorMessage";
        String SUCCESS = "successMessage";
    }

    public interface Menu {

        interface Bank {
            String MENU = "Bank";

            interface View360 {
                String SUB_MENU = "360 View";
                String BRANCHES = "Branches";
                String VAULT_GL = "Vaults/GL";
                String TRANSACTIONS = "All Transactions";
                String ACCOUNTS = "All Accounts";
            }

            interface Users {
                String SUB_MENU = "Users";
                String ROOT = "All Users";
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
            String ROOT = "Transactions List";
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
            String REQUEST = "Request Loan";
            String REQUESTS = "Loan Requests";
        }

        interface Card {
            String MENU = "Cards";
            String ROOT = "All Cards";
            String REQUESTS = "Card Requests";
        }

        interface Account {
            String MENU = "Accounts";
            String ROOT = "Accounts List";
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