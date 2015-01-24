package com.example.eric.fourfunctioncalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class MainActivity extends ActionBarActivity {
    // Data Members
    private StringBuilder mCurrentOperand = null;
    private BigDecimal mLeftOperand = null;
    private char mOperator = ' ';
    private boolean mClickedOperator = false;
    private TextView mInputTextRepresentation = null;
    private TextView mLeftOperandTextRepresentation = null;

    // Keys used for SharedPreferences
    static final String CURRENT_OPERAND_KEY = "GotThat";
    static final String LEFT_OPERAND_KEY = "CantStop";
    static final String OPERATOR_KEY = "WontStop";
    static final String CLICKED_OPERATOR_KEY = "YaFeeeel";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mInputTextRepresentation = (TextView) findViewById(R.id.currentOperandTextView);
        this.mLeftOperandTextRepresentation = (TextView) findViewById(R.id.leftOperandTextView);
        this.mClickedOperator = getPreferences(MODE_PRIVATE).getBoolean(CLICKED_OPERATOR_KEY, false);
        this.mCurrentOperand = new StringBuilder(getPreferences(MODE_PRIVATE).getString(CURRENT_OPERAND_KEY, "0"));
        this.mLeftOperand = new BigDecimal(getPreferences(MODE_PRIVATE).getString(LEFT_OPERAND_KEY, "0"));
        this.mOperator = getPreferences(MODE_PRIVATE).getString(OPERATOR_KEY, " ").charAt(0);
        updateCalculatorDisplay();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(CURRENT_OPERAND_KEY, this.mCurrentOperand.toString());
        editor.putString(LEFT_OPERAND_KEY, this.mLeftOperand.toString());
        editor.putString(OPERATOR_KEY, Character.toString(this.mOperator));

        editor.apply();
    }

    /** Button Click Handlers **/
    public void clickButtonNumberHandler(View view) {
        CharSequence value = ((TextView) view).getText();
        if (this.mCurrentOperand.toString().equals("0"))
            this.mCurrentOperand.setCharAt(0, value.charAt(0));
        else
            this.mCurrentOperand.append(value);

        updateCalculatorDisplay();
    }

    public void clickButtonEqualHandler(View view) {
        if (this.mClickedOperator && divideByZero()) {
            displayToast(getResources().getString(R.string.divide_by_zero_toast));
            return;
        }
        else if (this.mClickedOperator) {
            setOperandsAndOperator(this.mLeftOperand.toString(), this.mOperator,
                    calculate(this.mLeftOperand, this.mOperator, new BigDecimal(this.mCurrentOperand.toString())), false);
        }

        setOperandsAndOperator("0", ' ', this.mCurrentOperand, false);
        updateCalculatorDisplay();
    }

    public void clickButtonClearHandler(View view) {
        setOperandsAndOperator("0", ' ', "0", false);

        updateCalculatorDisplay();
    }

    public void clickButtonBackspaceHandler(View view) {
        int length = this.mCurrentOperand.length();
        String modified = (length > 1) ? this.mCurrentOperand.substring(0, length-1) : "0";
        setOperandsAndOperator(this.mLeftOperand.toString(), this.mOperator, modified, this.mClickedOperator);
        updateCalculatorDisplay();
    }

    public void clickButtonOperatorsHandler(View view) {
        char operator = ((TextView) view).getText().charAt(0);
        if(!this.mClickedOperator)
            setOperandsAndOperator(this.mCurrentOperand, operator, "0", true);
        else
            setOperandsAndOperator(this.mLeftOperand.toString(), operator, this.mCurrentOperand, true);

        updateCalculatorDisplay();
    }

    public void clickButtonDecimalHandler(View view) {
        char decimal = ((TextView) view).getText().charAt(0);

        if (this.mCurrentOperand.indexOf(".") == -1) {
            this.mCurrentOperand.append(decimal);
            updateCalculatorDisplay();
        }
    }
    /** END BUTTON CLICK HANDLERS **/

    /** Helper Methods **/
    private void updateCalculatorDisplay() {
        // Set the LittleTextView.
        CharSequence left = formatNumber(this.mLeftOperand.toString());
        CharSequence right = formatNumber(this.mCurrentOperand);

        if (this.mClickedOperator) {
            this.mLeftOperandTextRepresentation.setText(left.toString() + " " + this.mOperator);
        }
        else {
            this.mLeftOperandTextRepresentation.setText(left.toString());
        }

        this.mInputTextRepresentation.setText(right.toString());
    }

    private String calculate(BigDecimal left, char operator, BigDecimal right) {
        BigDecimal result;
        switch (operator) {
            case '+':
                result =  left.add(right);
                break;
            case '-':
                result = left.subtract(right);
                break;
            case '*':
                result = left.multiply(right);
                break;
            case '/':
                result = left.divide(right, 7, BigDecimal.ROUND_HALF_UP);
                break;
            default:
                result = BigDecimal.ZERO;
                break;
        }

        return result.toString();
    }

    private void setOperandsAndOperator(CharSequence left, char op, CharSequence right, boolean clicked) {
        this.mLeftOperand = null;
        this.mLeftOperand = new BigDecimal(left.toString());
        this.mOperator = op;
        this.mCurrentOperand = null;
        this.mCurrentOperand = new StringBuilder(right);
        this.mClickedOperator = clicked;
    }

    private void displayToast(CharSequence dialog) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, dialog, duration);
        toast.show();
    }

    private boolean divideByZero() {
        BigDecimal right = new BigDecimal(this.mCurrentOperand.toString());
        try {
            calculate(this.mLeftOperand, this.mOperator, right);
        }
        catch (ArithmeticException e) {
            return true;
        }

        return false;
    }

    private CharSequence formatNumber(CharSequence seq) {
        BigDecimal value = new BigDecimal(seq.toString());
        DecimalFormat formatter = new DecimalFormat("1.####E0");

        if (seq.length() < 24) {
            return seq;
        }

        return formatter.format(value);
    }
    /** END HELPER METHOD **/
}
