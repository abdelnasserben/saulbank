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
                String ACCOUNTS = "Branch Accounts";
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
    }
}