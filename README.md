### Current setup contain 999 doctors, 1001 patients and over 10k visits for testing.
Known bugs: This setup is using Caching to reduce call to database and it has 30 mins ttl. Thats why some of newly added records is not visible in cached requests.
CacheEvict methods should be added, but i believe for test purposes it will not be a big concern. IF not - just disable caching or contact me.




## Steps to Launch Test Application

1. Clone the Repository using command
```git clone https://github.com/MaHyxa/testHospital.git```

2. Open the Project in IntelliJ IDEA.
- Launch IntelliJ IDEA. 
- Select File > Open and navigate to the folder where cloned project is located.
- Choose the project folder and click OK.

3. Import the Project Dependencies.

Maven IntelliJ IDEA should automatically detect the project and prompt you to import the dependencies.

Wait for IntelliJ IDEA to finish indexing and downloading dependencies, or you can manually trigger it by right-clicking the pom.xml file and selecting Add as Maven Project

4. Configure the Application. (If Needed)

You might need to configure some variables, such as database credentials, or any other configuration in the application.yml file.

Check application.yml for database connection credential and change it depends on your local MySQL setup.

5. Import database dump file into your local MySQL.

- Use command line or MySQL Workbench to create new database.

   ```CREATE DATABASE hospital;```
   
- Import the Dump File:
  - Import dump file into the newly created database using the following command:</br>```mysql -u your_username -p hospital < /path/to/your/database_dump.sql```
  - Replace your_username with your MySQL username.
  - Replace /path/to/your/database_dump.sql with the full path to dump file located in this setup.

6. Run the Application
- Find the TestHospitalApplication.java file and run main method.

7. Access the Application
- Open Postman or any its alternatives and test 2 endpoints on `localhost:8080`
```
localhost:8080/api/hospital/createVisit
localhost:8080/api/hospital/patientVisits
```
