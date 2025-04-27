# Demo JDBC CRUD Console Application

This project is a simple Java console application that demonstrates full CRUD (Create, Read, Update, Delete, and Search) operations on a PostgreSQL database using JDBC with PreparedStatements.

The goal of this application is to serve as a reference for basic database interactions through Java, using clean code structure, input handling, and secure database access practices.

---

## Features

- Create the `Orders` table automatically if it does not exist
- Insert new orders with user input
- Update the order status by Customer ID
- Display all orders with formatted output
- Delete an order by Order ID
- Search orders by Customer ID
- Use of `PreparedStatement` to prevent SQL injection
- Console-based menu for interaction
- Input validation to prevent application crashes

---

## Requirements

- Java 8 or higher
- PostgreSQL database installed locally or remotely
- PostgreSQL JDBC Driver (e.g., `postgresql-42.x.x.jar`)

---
