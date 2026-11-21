
package com.example.calculatorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    TextView tvDisplay, tvHistory;

    double firstNumber = 0;
    double lastSecondNumber = 0;

    String operator = "";
    String lastOperator = "";

    boolean newNumber = true;
    boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect display
        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);

        // Number buttons
        int[] numberButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2,
                R.id.btn3, R.id.btn4, R.id.btn5,
                R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numberButtonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(this::numberClick);
        }

        // Decimal button
        findViewById(R.id.btnDot).setOnClickListener(this::numberClick);

        // Double zero
        findViewById(R.id.btn00).setOnClickListener(v -> doubleZeroClick());

        // Arithmetic operators
        findViewById(R.id.btnAdd).setOnClickListener(v -> operatorClick("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> operatorClick("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> operatorClick("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> operatorClick("÷"));

        // Equals
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());

        // Clear
        findViewById(R.id.btnClear).setOnClickListener(v -> clear());

        // Backspace
        findViewById(R.id.btnBack).setOnClickListener(v -> backspaceClick());

        // Sign toggle
        findViewById(R.id.btnSign).setOnClickListener(v -> signClick());
    }

    // Number and decimal input
    void numberClick(View view) {

        if (error) {
            clear();
        }

        Button button = (Button) view;
        String input = button.getText().toString();
        String current = tvDisplay.getText().toString();

        if (newNumber) {

            if (input.equals(".")) {
                tvDisplay.setText("0.");
            } else {
                tvDisplay.setText(input);
            }

            newNumber = false;

        } else {

            if (input.equals(".")) {

                if (!current.contains(".")) {
                    tvDisplay.setText(current + ".");
                }

            } else {

                if (current.equals("0")) {
                    tvDisplay.setText(input);
                } else if (current.equals("-0")) {
                    tvDisplay.setText("-" + input);
                } else {
                    tvDisplay.setText(current + input);
                }
            }
        }

        // Update expression while typing
        updateExpression();
    }

    // Double zero button
    void doubleZeroClick() {

        if (error) {
            clear();
        }

        String current = tvDisplay.getText().toString();

        if (newNumber) {
            tvDisplay.setText("0");
            newNumber = false;
        } else if (current.equals("0")) {
            tvDisplay.setText("0");
        } else if (current.equals("-0")) {
            tvDisplay.setText("-0");
        } else {
            tvDisplay.setText(current + "00");
        }

        updateExpression();
    }

    // Operator handling
    void operatorClick(String op) {

        if (error) {
            clear();
            return;
        }

        double currentValue = getDisplayValue();

        // Calculate chained operations
        if (!operator.isEmpty() && !newNumber) {

            double result = performCalculation(
                    firstNumber,
                    currentValue,
                    operator
            );

            if (Double.isNaN(result) || Double.isInfinite(result)) {
                showError();
                return;
            }

            tvDisplay.setText(formatResult(result));
            firstNumber = result;

        } else {

            firstNumber = currentValue;
        }

        operator = op;
        newNumber = true;

        // Display expression
        tvHistory.setText(formatResult(firstNumber) + " " + operator);
    }

    // Update expression preview
    void updateExpression() {

        if (!operator.isEmpty()) {

            tvHistory.setText(
                    formatResult(firstNumber) + " " +
                            operator + " " +
                            tvDisplay.getText().toString()
            );
        }
    }

    // Calculate result
    void calculate() {

        if (error) {
            return;
        }

        // Normal calculation
        if (!operator.isEmpty()) {

            double secondNumber = getDisplayValue();

            double result = performCalculation(
                    firstNumber,
                    secondNumber,
                    operator
            );

            if (Double.isNaN(result) || Double.isInfinite(result)) {
                showError();
                return;
            }

            // Show complete expression
            tvHistory.setText(
                    formatResult(firstNumber) + " " +
                            operator + " " +
                            formatResult(secondNumber) + " ="
            );

            // Save for repeated equals
            lastOperator = operator;
            lastSecondNumber = secondNumber;

            tvDisplay.setText(formatResult(result));

            firstNumber = result;
            operator = "";
            newNumber = true;
        }

        // Repeated equals
        else if (!lastOperator.isEmpty() && newNumber) {

            double current = getDisplayValue();

            double result = performCalculation(
                    current,
                    lastSecondNumber,
                    lastOperator
            );

            if (Double.isNaN(result) || Double.isInfinite(result)) {
                showError();
                return;
            }

            tvHistory.setText(
                    formatResult(current) + " " +
                            lastOperator + " " +
                            formatResult(lastSecondNumber) + " ="
            );

            tvDisplay.setText(formatResult(result));

            firstNumber = result;
            newNumber = true;
        }
    }

    // Mathematical operations
    double performCalculation(double a, double b, String op) {

        switch (op) {

            case "+":
                return a + b;

            case "-":
                return a - b;

            case "×":
                return a * b;

            case "÷":
                if (b == 0) {
                    return Double.NaN;
                }
                return a / b;

            default:
                return b;
        }
    }

    // Clear calculator
    void clear() {

        tvDisplay.setText("0");
        tvHistory.setText("");

        firstNumber = 0;
        lastSecondNumber = 0;

        operator = "";
        lastOperator = "";

        newNumber = true;
        error = false;
    }

    // Backspace
    void backspaceClick() {

        if (error) {
            clear();
            return;
        }

        if (newNumber) {
            tvDisplay.setText("0");
            newNumber = false;
            updateExpression();
            return;
        }

        String current = tvDisplay.getText().toString();

        if (current.length() > 1) {

            current = current.substring(0, current.length() - 1);

            if (current.equals("-") || current.isEmpty()) {
                current = "0";
            }

        } else {
            current = "0";
        }

        tvDisplay.setText(current);
        updateExpression();
    }

    // Positive / Negative sign
    void signClick() {

        if (error) {
            clear();
            return;
        }

        double value = getDisplayValue();

        if (value == 0) {
            return;
        }

        if (value > 0) {
            tvDisplay.setText("-" + formatResult(value));
        } else {
            tvDisplay.setText(formatResult(Math.abs(value)));
        }

        newNumber = false;
        updateExpression();
    }

    // Get number from display
    double getDisplayValue() {

        String value = tvDisplay.getText().toString();

        if (value.isEmpty() || value.equals("-") || value.equals(".")) {
            return 0;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Format numbers
    String formatResult(double value) {

        return BigDecimal.valueOf(value)
                .stripTrailingZeros()
                .toPlainString();
    }

    // Error handling
    void showError() {

        tvHistory.setText("");
        tvDisplay.setText("Cannot divide by 0");

        operator = "";
        lastOperator = "";

        firstNumber = 0;
        lastSecondNumber = 0;

        newNumber = true;
        error = true;
    }
}
