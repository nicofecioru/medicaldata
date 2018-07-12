package licenta.medicaldata.utils;

import java.text.DateFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A class for validating a ,
 * which is the Romanian national personal identification code.
 */
public class CnpValidator {

    /** The standard length of a CNP. */
    public static final int LENGTH = 13;

    private static final DateFormat CNP_DATE_FORMAT = new SimpleDateFormat("yyMMdd");

    private static int[] CONTROL_VALUES = new int[] {
            2, 7, 9, 1, 4, 6, 3, 5, 8, 2, 7, 9
    };

    private static int[] YEAR_OFFSET = new int[] {
            0, 1900, 1900, 1800, 1800, 2000, 2000
    };

    private static int[] getDigits(String cnp) {
        int[] digits = new int[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            char c = cnp.charAt(i);
            if (!Character.isDigit(c)) {
                return null;
            }
            digits[i] = (byte) Character.digit(c, 10);
        }
        return digits;
    }

    private static int getControlSum(int[] twelveDigits) {
        int k = 0;
        for (int i = 0; i < 12; i++) {
            k += CONTROL_VALUES[i] * twelveDigits[i];
        }
        k %= 11;
        if (k == 10) {
            k = 1;
        }
        return k;
    }

    /** Returns if the given string represents a valid CNP for the given birthdate.
     * The 2nd and the 3rd digits represent the last two digits from the year birthdate,
     * the 4th and 5th represent the month and the 7th and 8th the day.
     */
    public static boolean validateBirthdate(String cnp, Date birthdate) {
        return cnp.length() > 6 && cnp.substring(1, 7).equals(CNP_DATE_FORMAT.format(birthdate));
    }

    /** Returns if the given string represents a valid CNP. */
    public static boolean validateGender(String cnp, boolean male, Date birthdate) {
        if (cnp == null || cnp.length() < 1 || !Character.isDigit(cnp.charAt(0)))
            return false;
        int g1 = Character.digit(cnp.charAt(0), 10);
        Calendar c = new GregorianCalendar();
        c.setTime(birthdate);
        int g2 = c.get(Calendar.YEAR) < 2000
                ? male ? 1 : 2
                : male ? 5 : 6;
        return g1 == g2;
    }

    public static String create(boolean male, Date birthdate, int regionId, int birthRegisterNo) {
        StringBuffer result = new StringBuffer("0000000000000");

        Calendar c = new GregorianCalendar();
        c.setTime(birthdate);

        if (c.get(Calendar.YEAR) < 1900) {
            result.setCharAt(0, male ? '3' : '4');
        } else if (c.get(Calendar.YEAR) < 2000) {
            result.setCharAt(0, male ? '1' : '2');
        } else {
            result.setCharAt(0, male ? '5' : '6');
        }
        result.replace(1, 7, CNP_DATE_FORMAT.format(birthdate));

        result.replace(7, 9, new Integer(Math.abs(regionId) % 100).toString());
        result.replace(9, 12, new Integer(Math.abs(birthRegisterNo) % 1000).toString());

        int k = getControlSum(getDigits(result.toString()));
        result.replace(12, 13, new Integer(k).toString());

        return result.toString();
    }

    private static Random RANDOM = new Random();

    public static String createRandom(boolean male, Date birthdate) {
        return createRandom(male, birthdate, RANDOM);
    }

    public static String createRandom(boolean male, Date birthdate, Random random) {
        int departmentId = randomDigit(true, random) * 10 + randomDigit(false, random);
        int orderId = randomDigit(true, random) * 100 + randomDigit(false, random) * 10 + randomDigit(false, random);
        return create(male, birthdate, departmentId, orderId);
    }

    private static int randomDigit(boolean nonZero, Random random) {
        return nonZero
                ? random.nextInt(9) + 1
                : random.nextInt(10);
    }


    public static boolean isValid(String cnp) {
        if (cnp == null) {
            return false;
        }
        if (cnp.length() != LENGTH) {
            return false;
        }
        int[] cnpDigits = getDigits(cnp);
        if (cnpDigits == null) {
            return false;
        }
        if (cnpDigits[LENGTH - 1] != getControlSum(cnpDigits)) {
            return false;
        }

        // validate birthdate
        int month = cnpDigits[3] * 10 + cnpDigits[4];
        if (month < 1 && month > 12) {
            return false;
        }
        int dayOfMonth = cnpDigits[5] * 10 + cnpDigits[6];
        if (dayOfMonth < 1) {
            return false;
        }
        int year = YEAR_OFFSET[cnpDigits[0]] + cnpDigits[1] * 10 + cnpDigits[2];
        int maxDayOfMonth = new GregorianCalendar(year, month, dayOfMonth).getActualMaximum(Calendar.DAY_OF_MONTH);
        if (dayOfMonth > maxDayOfMonth) {
            return false;
        }

        return true;
    }

    private static void setMessage(ConstraintValidatorContext context, String message) {
        if (context == null) {
            return;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("cnp.invalid." + message);
    }

}
