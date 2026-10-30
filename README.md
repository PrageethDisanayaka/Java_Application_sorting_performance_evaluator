# Sorting Performance Evaluator

A Java Swing desktop application for sorting CSV data and comparing the performance of five sorting algorithms.

## Features

- Load CSV files and select a numeric column.
- Compare manually implemented Insertion, Shell, Merge, Quick and Heap Sort.
- Display execution times and identify the fastest algorithm.
- Preview sorted records while preserving their associated fields.
- Export sorted data as CSV.
- Handle invalid files, missing values and nonnumeric input.

## Requirements

- **JDK 17 or newer** to run from source and execute tests.
- **Java 17 or newer** to run the executable JAR.
- Windows for the included batch scripts.

No external Java libraries, Maven, Gradle or database are required.

## Getting Started

1. Download and extract the repository ZIP.
2. Double-click **`run.bat`** to compile and launch the application.

Alternatively, run the executable JAR:

```bat
java -jar SortingPerformanceEvaluator.jar
```

## How to Use

1. Click **Open CSV file** and select a dataset.
2. Choose a numeric column.
3. Select the number of timed trials.
4. Click **Compare Algorithms**.
5. Select a result to preview its sorted records.
6. Click **Export sorted CSV** to save the output.

Try `data/students.csv` and select **Marks** for a quick demonstration.

## Performance Results

The application reports the median sorting time from **3, 5 or 7 trials**, following one warm-up run. Each algorithm starts with the same original record order.

Lower times are faster. Results vary by dataset and computer, and exact timing ties are supported.

## CSV Requirements

Use a UTF-8 CSV file with unique, nonempty column headers and a consistent number of fields per row. Every value in the selected sorting column must be numeric and finite.

- Maximum file size: **50 MB**
- Preview limit: **500 rows**
- Sorting and export include **all rows**

## Project Structure

```text
├── src/                              # Java application source
├── test/                             # Automated tests
├── data/                             # Sample CSV files
├── run.bat                           # Compile and launch
├── test.bat                          # Compile and run tests
├── SortingPerformanceEvaluator.jar   # Executable application
├── README.md
└── .gitignore
```

## Testing

Run the correctness tests:

```bat
test.bat
```

After compilation, run the optional GUI tests:

```bat
java -cp build sortapp.GuiSmokeTest
```

## Troubleshooting

- **Java commands are not recognized:** Install JDK 17 or newer and add its `bin` folder to your Windows `PATH`.
- **A column cannot be sorted:** Correct the missing or invalid values, or select another numeric column.
- **Sorting takes a long time:** Try a smaller dataset; Insertion Sort can be slow on large inputs.
