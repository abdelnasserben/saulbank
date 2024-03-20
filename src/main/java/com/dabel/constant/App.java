package com.dabel.constant;

public class App {
    public interface Endpoint {
        String BRANCH_ROOT = "/branches";
        String BRANCH_ACCOUNTS = BRANCH_ROOT + "/accounts";

        String CUSTOMER_ROOT = "/customers";
        String CUSTOMER_ADD = CUSTOMER_ROOT + "/add";

        String TRANSACTION_ROOT = "/transactions";
        String TRANSACTION_INIT = TRANSACTION_ROOT + "/init";
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

            interface Basics {

                String SUB_MENU = "Basics";
                String ROOT = "All Transactions";
                String INIT = "Deposit/Withdraw";
            }
        }
    }
}