# Careflow

Careflow is a modern, role-based hospital management system built with Java 21, JavaFX, and SQLite. It provides a robust platform for managing patients, appointments, medical records, billing, and more, with dedicated interfaces for doctors, nurses, patients, and admins.

---

## Features

- **Role-Based Dashboards:** Separate interfaces for doctors, nurses, patients, and admins.
- **Patient Management:** Register, view, and update patient information.
- **Appointment Scheduling:** Book, view, and manage appointments.
- **Medical Records:** Track vitals, allergies, conditions, and visit notes (with draft/final/archived states).
- **Prescription Management:** Create and manage prescriptions.
- **Billing:** Integrated billing and financial records.
- **Excel Export:** Download patient lists and other data as Excel files.
- **Modern UI:** Built with JavaFX, FXML, and custom CSS for a responsive, card-based interface.
- **Secure Authentication:** Role-based login and permissions.
- **Database:** Local SQLite database (no server required).

---

## Project Structure

```
.
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/careflow/
│   │   │       ├── Careflow.java         # Main application entry point
│   │   │       ├── manager/              # Database management
│   │   │       ├── repository/           # DAOs and entities
│   │   │       ├── presentation/         # Controllers (role-based)
│   │   │       └── service/              # Business logic/services
│   │   └── resources/
│   │       ├── database.db               # SQLite database file
│   │       └── edu/careflow/
│   │           ├── fxml/                 # FXML UI layouts (role-based)
│   │           ├── css/                  # Stylesheets
│   │           ├── images/               # Icons and images
│   │           └── fonts/                # Custom fonts
└── ...
```

---

## Build & Run

### Prerequisites

- Java 21 (JDK)
- Gradle (wrapper included, no need to install separately)

### Build

```sh
./gradlew build
```

### Run

```sh
./gradlew run
```

Or, to run the optimized JLink image:

```sh
./gradlew jlinkZip
# Unzip the distribution from build/distributions/
# Then run the launcher inside the unzipped folder
```

---

## Configuration

- **Database:**  
  The SQLite database is located at `src/main/resources/database.db`.  
  Connection URL: `jdbc:sqlite:src/main/resources/database.db`

- **Main Class:**  
  `edu.careflow.Careflow` (launches the JavaFX application)

- **FXML & UI:**  
  All FXML files are under `src/main/resources/edu/careflow/fxml/`, organized by user role.

- **Styles:**  
  Custom CSS is in `src/main/resources/edu/careflow/css/styles.css`.

---

## Key Dependencies

- JavaFX 21 (controls, fxml, swing, web)
- MySQL Connector/J (for optional MySQL support)
- SQLite JDBC
- ControlsFX, GemsFX, CalendarFX, Ikonli (UI components)
- Apache POI (Excel export)
- JUnit 5 (testing)

See `build.gradle` for the full list.

---

## Architecture

- **Presentation Layer:**  
  Controllers in `edu.careflow.presentation.controllers`, organized by role (doctor, nurse, patient, admin).
- **Business Layer:**  
  Services in `edu.careflow.service`.
- **Data Access Layer:**  
  DAOs in `edu.careflow.repository.dao`.
- **Entity Layer:**  
  Models in `edu.careflow.repository.entities`.

See the [Controller Architecture Guide](#) and [Database Configuration Guide](#) for more details.

---

## Usage

1. **Login:**  
   Launch the app and log in with your credentials (role-based access).

2. **Navigation:**  
   Use the dashboard to access patient lists, appointments, medical records, and more.

3. **Export Data:**  
   Use the "Download" button in lists to export data to Excel.

4. **Role Features:**  
   - **Doctors:** Manage patients, appointments, and visit notes.
   - **Nurses:** View patient lists, record vitals.
   - **Patients:** View profile, appointments, prescriptions.
   - **Admins:** Manage users and permissions.

---

## Development

- **Add new controllers** under the appropriate role directory in `presentation/controllers/`.
- **Add new FXML views** in the corresponding `fxml/` subfolder.
- **Update the database schema** in `src/main/resources/database.db` and DAOs as needed.

---

## Testing

Run all tests with:

```sh
./gradlew test
```

---

## License

[MIT License](LICENSE)

---

## Credits

- JavaFX, ControlsFX, GemsFX, CalendarFX, Ikonli, Apache POI, and all open-source libraries used.

---

**For more details, see the in-code documentation and guides.** 