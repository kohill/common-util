package com.healthedge.common;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.javamoney.moneta.function.MonetaryOperators;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

public class Dollar {

    private static final CurrencyUnit CURRENCY_UNIT = Monetary.getCurrency(Locale.US);
    private static final MonetaryAmountFormat DEFAULT_FORMATTER = MonetaryFormats.getAmountFormat(AmountFormatQueryBuilder.of(Locale.US).set(CurrencyStyle.SYMBOL).build());
    public static final Dollar ZERO = new Dollar();
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private MonetaryAmount value;

    public Dollar() {
        this.value = Money.zero(CURRENCY_UNIT);
    }

    public Dollar(double amount) {
       this(amount, RoundingMode.UP, 2);
    }

    public Dollar(double amount, RoundingMode roundingMode) {
        this(amount, roundingMode, 2);
    }

    public Dollar(double amount, RoundingMode roundingMode, int scale) {
        BigDecimal bdAmount = new BigDecimal(amount);
        this.value = Money.of(bdAmount, CURRENCY_UNIT).with(MonetaryOperators.rounding(roundingMode, scale));
    }

    public Dollar(String amount) {
        this(amount, RoundingMode.UP, 2);
    }

    public Dollar(String amount, RoundingMode roundingMode) {
        this(amount, roundingMode, 0);
    }

    public Dollar(String amount, RoundingMode roundingMode, int scale) {
        BigDecimal bdAmount;
        try {
            bdAmount = new BigDecimal(amount.replace("(", "-").replace("$", "").replace(")", "").replace(",", ""));
        } catch (NumberFormatException e) {
            throw new DollarException("Unable to convert '" + amount + "' to a Dollar value");
        }
        this.value = Money.of(bdAmount, CURRENCY_UNIT).with(MonetaryOperators.rounding(roundingMode, scale));
    }

    private Dollar(MonetaryAmount amount) {
        this.value = amount;
    }

    public Dollar add(Dollar amount) {
        MonetaryAmount newAmount = this.value.add(amount.value);
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar add(double amount) {
        MonetaryAmount newAmount = this.value.add(Money.of(new BigDecimal(amount), CURRENCY_UNIT));
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar subtract(Dollar amount) {
        MonetaryAmount newAmount = this.value.subtract(amount.value);
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar subtract(double amount) {
        MonetaryAmount newAmount = this.value.subtract(Money.of(new BigDecimal(amount), CURRENCY_UNIT));
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar multiply(Dollar amount) {
        MonetaryAmount newAmount = this.value.multiply(amount.value.getNumber());
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar multiply(double amount) {
        MonetaryAmount newAmount = this.value.multiply(amount);
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar divide(Dollar amount) {
        MonetaryAmount newAmount = this.value.divide(amount.value.getNumber());
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar divide(double amount) {
        MonetaryAmount newAmount = this.value.divide(amount);
        return new Dollar(newAmount.with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar absoluteValue() {
        return new Dollar(this.value.abs().with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public Dollar negate() {
        return new Dollar(this.value.negate().with(MonetaryOperators.rounding(RoundingMode.UP, 2)));
    }

    public boolean equals(Dollar amount) {
        return this.value.equals(amount.value);
    }

    public boolean isLessThan(Dollar amount) {
        return this.value.isLessThan(amount.value);
    }

    public boolean isMoreThan(Dollar amount) {
        return this.value.isGreaterThan(amount.value);
    }

    public boolean isPositive() {
        return this.value.isPositive();
    }

    public String toString() {
        return decimalFormat.format(BigDecimal.valueOf(this.value.getNumber().doubleValue()));
    }

    public String format() {
        String formattedValue = DEFAULT_FORMATTER.format(value);
        return value.isNegative() ? String.format("(%s)", formattedValue).replace("-", "") : formattedValue;
    }

    public Dollar roundDown() {
        return roundDown(0);
    }

    public Dollar roundDown(int scale) {
        value = value.with(MonetaryOperators.rounding(RoundingMode.DOWN, scale));
        return this;
    }

    public Dollar roundUp() {
        return roundUp(0);
    }

    public Dollar roundUp(int scale) {
        value = value.with(MonetaryOperators.rounding(RoundingMode.DOWN, scale));
        return this;
    }

    private class DollarException extends RuntimeException {

        private static final long serialVersionUID = -6602327084203234347L;

        public DollarException(String message) {
            super(message);
        }

    }

}
