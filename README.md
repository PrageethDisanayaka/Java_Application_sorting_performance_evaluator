```markdown
# Sorting Performance Evaluator

A standalone Java Swing desktop application for benchmarking, comparing, and visualizing numeric sorting algorithms on CSV datasets.

Built entirely using standard Java SE with no external frameworks, dependencies, or database setup required.

---

## Folder Structure

```text
.
├── data/                            # Sample CSV files and test datasets
│   ├── benchmark-3000.csv
│   ├── edge-cases.csv
│   ├── invalid-missing.csv
│   ├── invalid-ragged.csv
│   └── students.csv
├── src/sortapp/                     # Java source code
│   ├── Main.java                    # Application entry point
│   ├── MainFrame.java               # Swing desktop UI
│   ├── TimingChart.java             # Execution time bar chart component
│   ├── UiTheme.java                 # UI styling and themes
│   └── ...                          # Sorting algorithms and CSV engine
├── tests/sortapp/                   # Correctness and GUI integration tests
├── run.bat                          # Automated build and run script (Windows)
├── test.bat                         # Automated test suite script (Windows)
├── SortingPerformanceEvaluator.jar  # Precompiled executable JAR
└── README.md                        # Project documentation

```

---

## Prerequisites

* **Java Development Kit (JDK) 17** or newer installed.
* Ensure `java` and `javac` are available in your system's PATH.

To verify your installation, open a terminal or command prompt and run:

```bash
java -version
javac -version

```

---

## Installation & Running

Choose one of the methods below to run the application:

### Option 1: Quick Run with Precompiled JAR

If you already have Java 17+ installed:

```bash
java -jar SortingPerformanceEvaluator.jar

```

### Option 2: Windows Batch Script

Double-click `run.bat` in the project root, or execute it in Command Prompt:

```cmd
run.bat

```

*(This automatically compiles the source code into a `build/` directory and launches the GUI).*

### Option 3: Manual Compilation (Cross-Platform)

From the root directory:

**Windows (Command Prompt):**

```cmd
if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build src/sortapp/*.java
java -cp build sortapp.Main

```

**macOS / Linux (Terminal):**

```bash
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/sortapp/*.java
java -cp build sortapp.Main

```

### Option 4: In an IDE (IntelliJ IDEA / Eclipse / VS Code)

1. Open or import the project directory.
2. Mark the `src` folder as the **Sources Root**.
3. Set your project SDK to **JDK 17+**.
4. Run the main class: `sortapp.Main`.

---

## How to Use

1. **Launch the Application:** Start the program using any of the methods above.
2. **Open a CSV File:**
* Click the **Open CSV** button.
* Choose a dataset (you can find samples in the `data/` directory, such as `students.csv`).


3. **Select a Numeric Column:**
* In the column dropdown, select any column marked `[numeric]` (non-numeric text columns cannot be sorted).


4. **Compare Algorithms:**
* Choose your desired number of benchmark trials (3, 5, or 7).
* Click **Compare Algorithms**.
* The application runs Insertion Sort, Shell Sort, Merge Sort, Quick Sort, and Heap Sort in the background.


5. **Inspect & Export Results:**
* View execution times (in milliseconds and nanoseconds) in the results table and bar chart. The fastest algorithm is highlighted.
* Click on any algorithm row in the results table to view its sorted data preview.
* Click **Export Sorted CSV** to save the fully sorted records to a new file.



---

## Running Tests

To run the automated validation test suite:

**Windows:**

```cmd
test.bat

```

**Manual Test Execution:**

```bash
javac --release 17 -encoding UTF-8 -d build src/sortapp/*.java tests/sortapp/*.java
java -cp build sortapp.RunAllTests

```

---

## Rebuilding the JAR

If you make modifications to the source code and want to package a new JAR:

```bash
# 1. Compile
javac --release 17 -encoding UTF-8 -d build src/sortapp/*.java

# 2. Package
jar --create --file SortingPerformanceEvaluator.jar --main-class sortapp.Main -C build .

```

```

```
