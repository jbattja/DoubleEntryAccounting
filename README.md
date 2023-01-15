# DoubleEntryAccounting
Application to do double entry bookkeeping

Application itself is a backend written in Java Spring which does the bookings and connects to a database, but also includes a front-end using Vaadin. 

To run: 
- Create a (mysql) database locally and update details in application.properties.
- Have maven download all required resources. 
- On the first run, all db tables will be created with some demo values. If a db reset is needed, just drop all tables, which re-triggers this process.
- Vaadin will launch on localhost:8080 for the UI. 
- Once the app is opened for the first time all frontend modules will be downloaded: this might take a couple of minutes.
