# Secure Password Generator

A desktop Java Swing application to generate strong, secure, and customizable passwords with an interactive user interface. The app uses cryptographically secure random generation and supports several password options including pronounceable mode.

## Features

- Generate passwords with options for uppercase, lowercase, numbers, and symbols
- Exclude similar characters (e.g., `l`, `1`, `O`, `0`) to avoid confusion
- Pronounceable password mode for easy-to-remember passwords
- Password length adjustable via slider from 8 to 32 characters
- Mask/unmask password display with toggle button
- Password strength meter with progress bar based on entropy calculation
- Copy generated password to clipboard with confirmation
- Automatically save generated passwords to a local password history file

## Installation and Usage

### Prerequisites

- Java SE Development Kit 8 or higher installed
- Any Java IDE (e.g., IntelliJ IDEA, Eclipse) or command line for compilation

### Running the Application

1. Clone or download this repository.
2. Open the project folder in your IDE or navigate there using terminal.
3. Compile the source code:
       javac PasswordGeneratorApp.java
4. Run the application:
       java PasswordGeneratorApp
5. Use the GUI to customize and generate secure passwords.

## How It Works

- The app uses `SecureRandom` for unpredictable password generation.
- Ensures passwords contain at least one character from each selected category.
- Checks for simple character sequences and avoids repeating adjacent characters.
- Calculates a password entropy score and displays a strength meter.
- Saves each generated password into `password_history.txt` in the application folder for tracking.


## Author
NAVAMOHAN M
navamohan5219@gmail.com

## Acknowledgments

Inspired by best practices in password security and GUI design.


