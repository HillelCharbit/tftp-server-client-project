# TFTP Client-Server Project

This project implements a robust TFTP (Trivial File Transfer Protocol) client-server application. Designed to facilitate efficient file transfers, the application enables clients to interact with the server to perform various operations such as uploading, downloading, and deleting files. The implementation strictly adheres to TFTP protocol specifications, ensuring compatibility with standard TFTP clients and servers.

## Table of Contents

1. [Features](#features)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Class Descriptions](#class-descriptions)
5. [Configuration](#configuration)
6. [Additional Notes](#additional-notes)
7. [Contributing](#contributing)
8. [License](#license)

---

## Features

- **TFTP Protocol Adherence**: Implements the TFTP protocol according to RFCs, ensuring interoperability with other TFTP clients and servers.
- **File Operations**: Supports essential TFTP operations:
  - **Read Request (RRQ)**: Download files from the server.
  - **Write Request (WRQ)**: Upload files to the server.
  - **Data (DATA)**: Transfer file data in blocks.
  - **Acknowledgment (ACK)**: Confirm successful block transfers.
  - **Error (ERROR)**: Handle and report error conditions.
  - **Directory Request (DIRQ)**: List files in the server’s directory.
  - **Login Request (LOGRQ)**: Authenticate clients with the server.
  - **Delete Request (DELRQ)**: Remove files from the server.
  - **Disconnect (DISC)**: Gracefully terminate client connections.
  - **Broadcast (BCAST)**: Notify clients of file changes (addition/deletion).
- **Client-Server Architecture**: Uses distinct client and server components.
- **Concurrent Connections**: Handles multiple clients concurrently through threading.
- **Error Handling**: Comprehensive mechanisms address protocol violations and file access issues.
- **Customizable Configuration**: Configure server parameters (e.g., port) through a configuration file.
- **Logging**: Logs server activities for debugging and monitoring.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/HillelCharbit/tftp-server-client-project.git
   ```
2. Navigate to the project directory:
   ```bash
   cd tftp-server-client-project
   ```
3. Compile the Java files:
   ```bash
   javac -d bin src/*.java
   ```
4. Run the server:
   ```bash
   java -cp bin TftpServer
   ```
5. Run the client:
   ```bash
   java -cp bin TftpClient
   ```

## Usage

Once the server is running, clients can connect to it using a TFTP client application to perform the following operations:
- **Download**: Retrieve files from the server.
- **Upload**: Send files to the server.
- **Delete**: Remove files from the server directory.
- **Directory List**: View files available on the server.
- **Login**: Authenticate and connect to the server.
- **Disconnect**: Terminate the connection.

## Class Descriptions

### Frames
Defines the structure of TFTP frames, encapsulating TFTP protocol requests and responses:
- **ACK**: Represents an acknowledgment frame.
- **DATA**: Represents a data frame.
- **DELRQ**: Represents a delete request frame.
- **DIRQ**: Represents a directory request frame.
- **DISC**: Represents a disconnect frame.
- **ERROR**: Represents an error frame.
- **LOGRQ**: Represents a login request frame.
- **RRQ**: Represents a read request frame.
- **WRQ**: Represents a write request frame.
- **Frame**: An abstract base class for all TFTP frames.

### Key Classes
- **Keyboard**: Manages keyboard input from the client for selecting file operations.
- **Listener**: Listens for and handles incoming messages from the server.
- **SharedResources**: Holds resources shared between server threads to maintain state consistency.
- **TftpClient**: The primary class for client-side operations.
- **TftpEncoderDecoder**: Encodes and decodes TFTP frames to manage communication.
- **TftpProtocol**: Implements the core TFTP protocol logic.
- **TftpProtocolUtil**: Provides utility functions for TFTP operations.
- **TftpServer**: The primary class for server-side operations, managing client requests and file transactions.

## Configuration

Server settings can be adjusted in the `config.properties` file, including:
- **Server Port**: Specify the port on which the server will listen (default is 7777).
- **Directory Path**: Set the path for the server's file storage directory (default is `/Files/`).

## Additional Notes

- **Concurrency**: The server uses threading to manage multiple client connections simultaneously.
- **Error Handling**: Robust error handling is in place to manage protocol violations and file access issues.
- **Logging**: Server logs provide insights into server activity and help with debugging.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature/YourFeature
   ```
3. Commit your changes:
   ```bash
   git commit -am 'Add YourFeature'
   ```
4. Push the branch:
   ```bash
   git push origin feature/YourFeature
   ```
5. Open a Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
