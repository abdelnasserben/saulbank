package com.dabel.constant;

public final class Web {

    public interface Endpoint {
        String BRANCH_ROOT = "/branches";
        String BRANCH_ACCOUNTS = BRANCH_ROOT + "/accounts";

        String CUSTOMER_ROOT = "/customers";
        String CUSTOMER_ADD = CUSTOMER_ROOT + "/add";

        String TRANSACTION_ROOT = "/transactions";
        String TRANSACTION_INIT = TRANSACTION_ROOT + "/init";
        String TRANSACTION_APPROVE = TRANSACTION_ROOT + "/approve";
        String TRANSACTION_REJECT = TRANSACTION_ROOT + "/reject";

        String EXCHANGE_ROOT = "/exchanges";
        String EXCHANGE_INIT = EXCHANGE_ROOT + "/init";
        String EXCHANGE_APPROVE = EXCHANGE_ROOT + "/approve";
        String EXCHANGE_REJECT = EXCHANGE_ROOT + "/reject";

        String LOAN_ROOT = "/loans";
        String LOAN_INIT = LOAN_ROOT + "/init";
        String LOAN_APPROVE = LOAN_ROOT + "/approve";
        String LOAN_REJECT = LOAN_ROOT + "/reject";

        String CARD_ROOT = "/cards";
        String CARD_ACTIVATE = CARD_ROOT + "/activate";
        String CARD_DEACTIVATE = CARD_ROOT + "/deactivate";
        String CARD_REQUEST_ROOT = CARD_ROOT + "/requests";
        String CARD_REQUEST_APPROVE = CARD_REQUEST_ROOT + "/approve";
        String CARD_REQUEST_REJECT = CARD_REQUEST_ROOT + "/reject";

        String ACCOUNT_ROOT = "/accounts";
        String ACCOUNT_ACTIVATE = ACCOUNT_ROOT + "/activate";
        String ACCOUNT_DEACTIVATE = ACCOUNT_ROOT + "/deactivate";
        String ACCOUNT_AFFILIATION = ACCOUNT_ROOT + "/affiliation";

        String PAGE_404 = "/404";
    }

    public interface View {
        String BRANCH_LIST = "branches";
        String BRANCH_ACCOUNTS = "branches-accounts";
        String DASHBOARD = "dashboard";

        String CUSTOMER_LIST = "customers";
        String CUSTOMER_ADD = "customers-add";
        String CUSTOMER_DETAILS = "customers-details";

        String TRANSACTION_LIST = "transactions";
        String TRANSACTION_INIT = "transactions-init";
        String TRANSACTION_DETAILS = "transactions-details";

        String EXCHANGE_LIST = "exchanges";
        String EXCHANGE_INIT = "exchanges-init";
        String EXCHANGE_DETAILS = "exchanges-details";

        String LOAN_LIST = "loans";
        String LOAN_INIT = "loans-init";
        String LOAN_DETAILS = "loans-details";

        String CARD_LIST = "cards";
        String CARD_DETAILS = "cards-details";
        String CARD_REQUEST_LIST = "cards-requests";
        String CARD_APPLICATION_DETAILS = "cards-requests-details";

        String ACCOUNT_LIST = "accounts";
        String ACCOUNT_DETAILS = "accounts-details";
        String ACCOUNT_AFFILIATION = "accounts-affiliations";
        String ACCOUNT_AFFILIATION_ADD = "accounts-affiliations-add";

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
            String REQUEST_ROOT = "Card Requests";
        }

        interface Account {
            String MENU = "Accounts";
            String ROOT = "All Accounts";
            String AFFILIATION = "Manage Affiliation";
            String AFFILIATION_ADD = "Add Affiliate";
        }
    }
}