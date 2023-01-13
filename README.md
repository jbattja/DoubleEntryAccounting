# DoubleEntryAccounting
Application to do double entry bookkeeping

Application itself is a backend written in Java Spring which does the bookings and connects to a database, but also includes a front-end using Vaadin. 

To run: 
- Create a (mysql) database locally and update details in application.properties.
- Have maven download all required resources. 
- On first run, make sure to run initialSetup.init(); to create initial database entries, by uncommenting it in the AccountingApplication.Java. Remove it again for next runs. 
- Vaadin will launch on localhost:8080 for the UI. 
