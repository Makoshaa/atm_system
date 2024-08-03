# Automated Teller Machine (ATM) System

## Description
This project aims to develop a secure and efficient Automated Teller Machine (ATM) System using Java and Spring technologies. The system simulates common ATM functionalities such as account access, balance inquiries, deposits, withdrawals, and transfers between accounts. It demonstrates capabilities in user authentication, session management, and secure transaction processing.

## Business Case
With the increasing reliance on electronic banking services, ATMs play a crucial role in the banking ecosystem. This robust ATM system enhances customer satisfaction by providing quick and secure access to banking services outside of traditional banking hours. The project allows for understanding and implementing essential features of banking systems, emphasizing security, data integrity, and user interaction.

## Architecture & Database Description
- **Database Schema:**
    - Users: id, account_number, pin, balance
    - Transactions: id, type, amount, date, account_number
- **Backend Framework:** Spring Boot
- **Security:** Spring Security for authentication and securing transaction endpoints
- **Data Handling:** Spring Data JPA for transaction and user management
- **API:** RESTful services using Spring Web for transaction processing

## User Stories
### As a bank customer, I want to:
- Insert my card (simulate by entering an account number and PIN)
- View my current account balance
- Withdraw cash from my account
- Deposit money into my account
- Transfer funds to another account
- Log out securely from the ATM

### As a bank administrator, I want to:
- Monitor and log all ATM transactions
- Manage ATM system issues such as replenishing cash or addressing transaction errors
- Update account information and PINs securely

### As a system, I need to:
- Validate user credentials
- Handle sessions to ensure that customers' operations are secure and personal information is protected
- Process transactions accurately and securely, updating account balances in real-time
- Provide timely feedback to the user about the status of their transactions

## API Requirements
### Authentication and Session Management:
- POST /auth/login: Authenticate user and create a session
- POST /auth/logout: Terminate the session

### Account Operations:
- GET /accounts/balance: Check the current balance
- POST /accounts/deposit: Deposit an amount to the user's account
- POST /accounts/withdraw: Withdraw an amount from the user's account
- POST /accounts/transfer: Transfer amount to another account

### Admin Functions:
- GET /admin/transactions: View all transactions processed by the ATM



